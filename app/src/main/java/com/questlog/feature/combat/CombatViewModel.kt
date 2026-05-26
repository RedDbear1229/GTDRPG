package com.questlog.feature.combat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.CompleteTaskResult
import com.questlog.core.domain.usecase.CompleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CombatUiState(
    val isRolling: Boolean = false,
    val result: CombatResult? = null,
    val error: String? = null,
    val alreadyCompleted: Boolean = false,
    val leveledUp: Boolean = false,
)

sealed class CombatEvent {
    data class ShowSheet(val taskId: String) : CombatEvent()
}

@HiltViewModel
class CombatViewModel @Inject constructor(
    private val completeTask: CompleteTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CombatUiState())
    val uiState: StateFlow<CombatUiState> = _uiState.asStateFlow()

    fun rollDice(taskId: String) {
        if (_uiState.value.isRolling) return
        _uiState.update { it.copy(isRolling = true, result = null, error = null, alreadyCompleted = false) }
        viewModelScope.launch {
            when (val outcome = completeTask(taskId)) {
                is CompleteTaskResult.Success -> {
                    _uiState.update { it.copy(isRolling = false, result = outcome.combatResult) }
                }
                CompleteTaskResult.AlreadyCompleted -> {
                    _uiState.update { it.copy(isRolling = false, alreadyCompleted = true) }
                }
                is CompleteTaskResult.Error -> {
                    _uiState.update { it.copy(isRolling = false, error = outcome.message) }
                }
            }
        }
    }

    fun dismiss() = _uiState.update { CombatUiState() }
}
