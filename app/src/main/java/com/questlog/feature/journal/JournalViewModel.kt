package com.questlog.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class JournalFilters(
    val lifeArea: LifeArea? = null,
    val context: String? = null,
)

data class JournalStats(
    val today: Int = 0,
    val thisWeek: Int = 0,
    val thisMonth: Int = 0,
)

data class JournalUiState(
    val items: List<Task> = emptyList(),
    val stats: JournalStats = JournalStats(),
    val filters: JournalFilters = JournalFilters(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(JournalFilters())

    // 자정 경계는 ViewModel 생성 시점 기준 한 번 계산. 자정에 사용자가 화면을 갱신하면
    // ViewModel 재구성(process death / 탭 재선택)에서 자연 갱신. F3.1 의 자정 워커 도입 시
    // 그쪽 신호로 cold start 시키는 식으로 정교화 가능.
    private val todayRange = todayRange()
    private val weekRange = weekRange()
    private val monthRange = monthRange()

    private val stats: kotlinx.coroutines.flow.Flow<JournalStats> = combine(
        taskRepository.observeCompletedCount(todayRange.first, todayRange.second),
        taskRepository.observeCompletedCount(weekRange.first, weekRange.second),
        taskRepository.observeCompletedCount(monthRange.first, monthRange.second),
    ) { today, week, month -> JournalStats(today = today, thisWeek = week, thisMonth = month) }

    val uiState: StateFlow<JournalUiState> = combine(
        taskRepository.observeCompleted(),
        stats,
        filters,
    ) { items, stat, filter ->
        JournalUiState(
            items = applyFilters(items, filter),
            stats = stat,
            filters = filter,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun setLifeArea(area: LifeArea?) = filters.update { it.copy(lifeArea = area) }
    fun setContext(context: String?) = filters.update { it.copy(context = context) }
    fun clearFilters() { filters.value = JournalFilters() }

    private fun applyFilters(tasks: List<Task>, f: JournalFilters): List<Task> =
        tasks.asSequence()
            .filter { f.lifeArea == null || it.lifeArea == f.lifeArea }
            .filter { f.context == null || it.context?.contains(f.context) == true }
            .toList()

    private fun todayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    private fun weekRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val start = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = sunday.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    private fun monthRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val first = today.withDayOfMonth(1)
        val last = today.with(TemporalAdjusters.lastDayOfMonth())
        val start = first.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = last.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }
}
