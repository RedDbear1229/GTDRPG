package com.questlog.feature.questboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.Project
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.repository.ProjectRepository
import com.questlog.core.domain.repository.TaskRepository
import com.questlog.core.domain.usecase.PrioritizeQuestsUseCase
import com.questlog.core.domain.usecase.TodayBucketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class QuestBoardTab { TODAY, ACTIVE, PROJECTS }

data class QuestBoardFilters(
    val context: String? = null,
    val lifeArea: LifeArea? = null,
    val query: String = "",
)

data class QuestBoardUiState(
    val tab: QuestBoardTab = QuestBoardTab.TODAY,
    val today: List<Task> = emptyList(),
    val active: List<Task> = emptyList(),
    val projects: List<ProjectSummary> = emptyList(),
    val filters: QuestBoardFilters = QuestBoardFilters(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

data class ProjectSummary(
    val project: Project,
    val taskCount: Int,
    val completedCount: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuestBoardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val prioritize: PrioritizeQuestsUseCase,
    private val todayBucket: TodayBucketUseCase,
) : ViewModel() {

    private val tab = MutableStateFlow(QuestBoardTab.TODAY)
    private val filters = MutableStateFlow(QuestBoardFilters())
    private val errorFlow = MutableStateFlow<String?>(null)

    private val activeTasks = taskRepository.observeActive().distinctUntilChanged()

    private val projectsWithCounts = projectRepository.observeActive()
        .flatMapLatest { projects ->
            if (projects.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(projects.map { p -> taskRepository.observeByProject(p.id) }) { perProject ->
                    projects.mapIndexed { idx, project ->
                        val list = perProject[idx]
                        ProjectSummary(
                            project = project,
                            taskCount = list.size,
                            completedCount = list.count { it.completedAt != null },
                        )
                    }
                }
            }
        }
        .distinctUntilChanged()

    val uiState: StateFlow<QuestBoardUiState> = combine(
        tab,
        activeTasks,
        projectsWithCounts,
        filters,
        errorFlow,
    ) { currentTab, tasks, projects, filter, error ->
        val filtered = applyFilters(tasks, filter)
        QuestBoardUiState(
            tab = currentTab,
            today = prioritize(todayBucket(filtered, endOfTodayMillis())),
            active = prioritize(filtered),
            projects = projects,
            filters = filter,
            isLoading = false,
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuestBoardUiState())

    fun selectTab(target: QuestBoardTab) { tab.value = target }
    fun setQuery(query: String) = filters.update { it.copy(query = query) }
    fun setContext(context: String?) = filters.update { it.copy(context = context) }
    fun setLifeArea(area: LifeArea?) = filters.update { it.copy(lifeArea = area) }
    fun clearFilters() { filters.value = QuestBoardFilters() }
    fun clearError() { errorFlow.value = null }

    private fun applyFilters(tasks: List<Task>, f: QuestBoardFilters): List<Task> =
        tasks.asSequence()
            .filter { f.query.isBlank() || it.title.contains(f.query, ignoreCase = true) }
            .filter { f.context == null || it.context?.contains(f.context) == true }
            .filter { f.lifeArea == null || it.lifeArea == f.lifeArea }
            .toList()

    private fun endOfTodayMillis(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    }
}
