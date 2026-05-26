package com.questlog.feature.questboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.Project
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.repository.ProjectRepository
import com.questlog.core.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel(assistedFactory = ProjectDetailViewModel.Factory::class)
class ProjectDetailViewModel @AssistedInject constructor(
    @Assisted private val projectId: String,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val projectFlow = MutableStateFlow<Project?>(null)

    init {
        viewModelScope.launch {
            projectFlow.value = projectRepository.getById(projectId)
        }
    }

    val uiState: StateFlow<ProjectDetailUiState> = combine(
        projectFlow,
        taskRepository.observeByProject(projectId),
    ) { project, tasks ->
        ProjectDetailUiState(project = project, tasks = tasks, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectDetailUiState())

    @AssistedFactory
    interface Factory { fun create(projectId: String): ProjectDetailViewModel }
}
