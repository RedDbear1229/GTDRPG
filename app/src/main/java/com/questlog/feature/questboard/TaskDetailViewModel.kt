package com.questlog.feature.questboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val task: Task? = null,
    val title: String = "",
    val nextAction: String = "",
    val notes: String = "",
    val estimatedMinutes: Int? = null,
    val dueDate: Long? = null,
    val lifeArea: LifeArea = LifeArea.PERSONAL,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val notFound: Boolean = false,
    val error: String? = null,
)

sealed interface TaskDetailEvent {
    data object Saved : TaskDetailEvent
    data object Deleted : TaskDetailEvent
}

@HiltViewModel(assistedFactory = TaskDetailViewModel.Factory::class)
class TaskDetailViewModel @AssistedInject constructor(
    @Assisted private val taskId: String,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TaskDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val task = taskRepository.getById(taskId)
            if (task == null) {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
            } else {
                _uiState.update {
                    it.copy(
                        task = task,
                        title = task.title,
                        nextAction = task.nextAction.orEmpty(),
                        notes = task.notes.orEmpty(),
                        estimatedMinutes = task.estimatedMinutes,
                        dueDate = task.dueDate,
                        lifeArea = task.lifeArea,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun setTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun setNextAction(value: String) = _uiState.update { it.copy(nextAction = value) }
    fun setNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun setEstimatedMinutes(value: Int?) = _uiState.update { it.copy(estimatedMinutes = value) }
    fun setDueDate(value: Long?) = _uiState.update { it.copy(dueDate = value) }
    fun setLifeArea(value: LifeArea) = _uiState.update { it.copy(lifeArea = value) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun save() {
        val current = _uiState.value
        val original = current.task ?: return
        if (current.title.isBlank()) {
            _uiState.update { it.copy(error = "제목은 비울 수 없습니다") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                taskRepository.upsert(
                    original.copy(
                        title = current.title.trim(),
                        nextAction = current.nextAction.ifBlank { null },
                        notes = current.notes.ifBlank { null },
                        estimatedMinutes = current.estimatedMinutes,
                        dueDate = current.dueDate,
                        lifeArea = current.lifeArea,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.tryEmit(TaskDetailEvent.Saved)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "저장 실패") }
            }
        }
    }

    fun delete() {
        val current = _uiState.value
        current.task ?: return
        viewModelScope.launch {
            runCatching { taskRepository.softDelete(taskId) }
                .onSuccess { _events.tryEmit(TaskDetailEvent.Deleted) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "삭제 실패") } }
        }
    }

    @AssistedFactory
    interface Factory { fun create(taskId: String): TaskDetailViewModel }
}
