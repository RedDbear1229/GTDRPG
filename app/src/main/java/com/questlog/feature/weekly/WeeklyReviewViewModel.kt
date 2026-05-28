package com.questlog.feature.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.data.db.dao.CombatLogDao
import com.questlog.core.data.db.dao.WeeklyReviewDao
import com.questlog.core.data.db.entity.WeeklyReviewEntity
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.ClaudeRepository
import com.questlog.core.domain.repository.TaskRepository
import com.questlog.core.domain.usecase.GainXPUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class ReviewStep(
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
)

data class WeekStats(
    val completedCount: Int = 0,
    val xpGained: Long = 0,
    val critCount: Int = 0,
    val missCount: Int = 0,
    val unfinishedCount: Int = 0,
    val weekLabel: String = "",
)

data class WeeklyReviewUiState(
    val currentStep: Int = 0,
    val steps: List<ReviewStep> = emptyList(),
    val weekStats: WeekStats = WeekStats(),
    val isAlreadyDone: Boolean = false,
    val isComplete: Boolean = false,
    val xpAwarded: Long = 0,
    val isGeneratingAi: Boolean = false,
    val aiSummary: String? = null,
    val error: String? = null,
)

sealed class WeeklyReviewEvent {
    data class ReviewComplete(val xpGained: Long) : WeeklyReviewEvent()
}

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val weeklyReviewDao: WeeklyReviewDao,
    private val combatLogDao: CombatLogDao,
    private val taskRepository: TaskRepository,
    private val characterRepository: CharacterRepository,
    private val claudeRepository: ClaudeRepository,
    private val gainXP: GainXPUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReviewUiState())
    val uiState: StateFlow<WeeklyReviewUiState> = _uiState

    private val _events = MutableSharedFlow<WeeklyReviewEvent>()
    val events: SharedFlow<WeeklyReviewEvent> = _events.asSharedFlow()

    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch { loadWeekData() }
    }

    private suspend fun loadWeekData() {
        val today = LocalDate.now(zone)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val weekStart = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val weekEnd = sunday.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        val weekStartIso = monday.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val weekLabel = "${monday.monthValue}월 ${weekOfMonth(monday)}주차"

        val alreadyDone = weeklyReviewDao.getByWeekStart(weekStartIso) != null

        val completedCount = taskRepository.observeCompletedCount(weekStart, weekEnd).first()
        val xpGained = combatLogDao.sumXpBetween(weekStart, weekEnd)
        val critCount = combatLogDao.countCritHitsBetween(weekStart, weekEnd)
        val missCount = combatLogDao.countCritMissesBetween(weekStart, weekEnd)
        val unfinishedCount = taskRepository.observeActive().first().size

        val stats = WeekStats(
            completedCount = completedCount,
            xpGained = xpGained,
            critCount = critCount,
            missCount = missCount,
            unfinishedCount = unfinishedCount,
            weekLabel = weekLabel,
        )

        _uiState.update {
            it.copy(
                weekStats = stats,
                isAlreadyDone = alreadyDone,
                steps = buildSteps(stats),
            )
        }
    }

    private fun buildSteps(stats: WeekStats) = listOf(
        ReviewStep(
            title = "수집함 비우기",
            description = "수집함의 모든 항목을 처리하고 비웠나요?",
        ),
        ReviewStep(
            title = "활성 퀘스트 검토",
            description = "현재 진행 중인 퀘스트 ${stats.unfinishedCount}개를 검토했나요?",
        ),
        ReviewStep(
            title = "프로젝트 점검",
            description = "각 활성 프로젝트에 명확한 다음 행동이 정의되어 있나요?",
        ),
        ReviewStep(
            title = "이번 주 총결산",
            description = "이번 주 ${stats.completedCount}개 퀘스트를 완료했습니다. 되돌아보셨나요?",
        ),
        ReviewStep(
            title = "다음 주 계획",
            description = "다음 주에 집중할 3가지 핵심 목표를 정했나요?",
        ),
        ReviewStep(
            title = "AI 주간 요약",
            description = "AI 던전마스터에게 주간 요약 보고서를 받으시겠어요? (선택)",
        ),
    )

    fun checkCurrentStep() {
        _uiState.update { state ->
            val steps = state.steps.toMutableList()
            val idx = state.currentStep
            if (idx < steps.size) {
                steps[idx] = steps[idx].copy(isChecked = true)
            }
            state.copy(steps = steps)
        }
    }

    fun nextStep() {
        val state = _uiState.value
        val nextIdx = state.currentStep + 1
        if (nextIdx >= state.steps.size) {
            completeReview()
        } else {
            _uiState.update { it.copy(currentStep = nextIdx) }
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            if (state.currentStep > 0) state.copy(currentStep = state.currentStep - 1)
            else state
        }
    }

    fun generateAiSummary() {
        val state = _uiState.value
        if (state.isGeneratingAi) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true) }
            runCatching {
                val character = characterRepository.getActive() ?: return@launch
                val stats = state.weekStats
                claudeRepository.generateWeeklyReviewSummary(
                    character = character,
                    completedCount = stats.completedCount,
                    xpGained = stats.xpGained,
                    critCount = stats.critCount,
                    missCount = stats.missCount,
                    unfinished = stats.unfinishedCount,
                    weekLabel = stats.weekLabel,
                )
            }.onSuccess { summary ->
                _uiState.update { it.copy(aiSummary = summary, isGeneratingAi = false) }
            }.onFailure { e ->
                Timber.e(e, "AI 주간 요약 실패")
                _uiState.update { it.copy(isGeneratingAi = false, error = "AI 요약 생성에 실패했습니다.") }
            }
        }
    }

    private fun completeReview() {
        val state = _uiState.value
        if (state.isAlreadyDone) {
            _uiState.update { it.copy(isComplete = true, xpAwarded = 0) }
            return
        }
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now(zone)
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekStartIso = monday.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val entity = WeeklyReviewEntity(
                    weekStart = weekStartIso,
                    weekLabel = state.weekStats.weekLabel,
                    completedCount = state.weekStats.completedCount,
                    xpGained = state.weekStats.xpGained,
                    critCount = state.weekStats.critCount,
                    missCount = state.weekStats.missCount,
                    unfinishedCount = state.weekStats.unfinishedCount,
                    aiSummary = state.aiSummary,
                    xpReward = WeeklyReviewEntity.REWARD_XP,
                )
                val inserted = weeklyReviewDao.insert(entity)
                // inserted == -1 → 이미 이번 주 리뷰 완료 (IGNORE), XP 미지급
                if (inserted != -1L) {
                    gainXP(WeeklyReviewEntity.REWARD_XP)
                }
                inserted
            }.onSuccess { inserted ->
                val xpAwarded = if (inserted != -1L) WeeklyReviewEntity.REWARD_XP else 0L
                _uiState.update { it.copy(isComplete = true, xpAwarded = xpAwarded) }
                _events.emit(WeeklyReviewEvent.ReviewComplete(xpAwarded))
            }.onFailure { e ->
                Timber.e(e, "주간 리뷰 저장 실패")
                _uiState.update { it.copy(error = "저장에 실패했습니다. 다시 시도해주세요.") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun weekOfMonth(monday: LocalDate): Int {
        val firstMonday = monday.withDayOfMonth(1)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        val diff = (monday.toEpochDay() - firstMonday.toEpochDay()).toInt()
        return (diff / 7) + 1
    }
}
