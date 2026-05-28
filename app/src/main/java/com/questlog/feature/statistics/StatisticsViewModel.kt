package com.questlog.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.data.db.dao.CombatLogDao
import com.questlog.core.data.db.dao.D20Count
import com.questlog.core.data.db.dao.DayCount
import com.questlog.core.data.db.dao.DayXp
import com.questlog.core.data.db.dao.LifeAreaCount
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val character: Character? = null,
    // 전체 요약
    val totalCompleted: Int = 0,
    val totalXpEarned: Long = 0,
    val currentStreak: Int = 0,
    val totalCritHits: Int = 0,
    val totalCritMisses: Int = 0,
    // 주간 바 차트 (최근 7일)
    val weeklyDayCounts: List<DayCount> = emptyList(),
    // 일별 XP 라인 차트 (최근 7일)
    val dailyXpData: List<DayXp> = emptyList(),
    // D20 분포 히스토그램
    val d20Distribution: List<D20Count> = emptyList(),
    // 생활 영역 파이
    val lifeAreaCounts: List<LifeAreaCount> = emptyList(),
    // 스트릭 캘린더 (최근 28일 완료된 날짜 집합)
    val completedDates: Set<String> = emptySet(),
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val combatLogDao: CombatLogDao,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            runCatching {
                val zone = ZoneId.systemDefault()
                val now = System.currentTimeMillis()
                val since7d = LocalDate.now(zone).minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
                val since28d = LocalDate.now(zone).minusDays(27).atStartOfDay(zone).toInstant().toEpochMilli()

                val character = characterRepository.observeActive().first()

                val weeklyDayCounts = taskDao.getCompletedDailySince(since7d)
                val dailyXpData = combatLogDao.getDailyXpSince(since7d)
                val d20Distribution = combatLogDao.getD20Distribution()
                val lifeAreaCounts = taskDao.getCompletedByLifeArea()
                val completedDates = taskDao.getCompletedDatesSince(since28d).toSet()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        character = character,
                        totalCompleted = character?.totalQuestsCompleted ?: 0,
                        totalXpEarned = character?.totalXpEarned ?: 0,
                        currentStreak = character?.streakDays ?: 0,
                        totalCritHits = character?.totalCriticalHits ?: 0,
                        totalCritMisses = character?.totalCriticalMisses ?: 0,
                        weeklyDayCounts = weeklyDayCounts,
                        dailyXpData = dailyXpData,
                        d20Distribution = d20Distribution,
                        lifeAreaCounts = lifeAreaCounts,
                        completedDates = completedDates,
                    )
                }
            }.onFailure { e ->
                Timber.e(e, "StatisticsViewModel 로드 실패")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadAll()
    }
}
