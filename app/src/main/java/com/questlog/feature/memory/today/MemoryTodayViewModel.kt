package com.questlog.feature.memory.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.MemoryTodayState
import com.questlog.core.domain.model.TaskSummary
import com.questlog.core.domain.usecase.memory.GetTodayMemoryStateUseCase
import com.questlog.core.domain.usecase.memory.SaveMemoryUseCase
import com.questlog.core.domain.usecase.memory.SaveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class MemoryTodayUiEvent {
    object Saved : MemoryTodayUiEvent()
    object AlreadyExists : MemoryTodayUiEvent()
    data class Error(val message: String) : MemoryTodayUiEvent()
}

data class MemoryTodayUiState(
    val state: MemoryTodayState = MemoryTodayState.NoCompletions,
    val isSaving: Boolean = false,
)

@HiltViewModel
class MemoryTodayViewModel @Inject constructor(
    private val getTodayMemoryState: GetTodayMemoryStateUseCase,
    private val saveMemory: SaveMemoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryTodayUiState())
    val uiState: StateFlow<MemoryTodayUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MemoryTodayUiEvent>()
    val events: SharedFlow<MemoryTodayUiEvent> = _events.asSharedFlow()

    init {
        loadState()
    }

    fun refresh() = loadState()

    private fun loadState() {
        viewModelScope.launch {
            getTodayMemoryState()
                .catch { e ->
                    Timber.e(e, "MemoryTodayViewModel: 상태 로드 실패")
                    _events.emit(MemoryTodayUiEvent.Error(e.message ?: "오류가 발생했습니다"))
                }
                .collect { state ->
                    _uiState.update { it.copy(state = state) }
                }
        }
    }

    fun selectTask(taskSummary: TaskSummary) {
        val current = _uiState.value.state
        if (current is MemoryTodayState.Selecting) {
            _uiState.update { it.copy(state = MemoryTodayState.Writing(taskSummary)) }
        }
    }

    fun saveMemory(body: String) {
        val current = _uiState.value.state
        val selected = (current as? MemoryTodayState.Writing)?.selected ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (saveMemory(selected, body)) {
                is SaveResult.Success -> {
                    _events.emit(MemoryTodayUiEvent.Saved)
                    loadState()
                }
                is SaveResult.AlreadyExists -> {
                    _events.emit(MemoryTodayUiEvent.AlreadyExists)
                    loadState()
                }
                is SaveResult.NoCharacter -> {
                    _events.emit(MemoryTodayUiEvent.Error("활성 캐릭터가 없습니다. 캐릭터를 먼저 생성해주세요."))
                }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun backToSelecting() {
        val candidates = (_uiState.value.state as? MemoryTodayState.Writing)?.selected
            ?: return
        // 후보 목록 재로드
        loadState()
    }
}
