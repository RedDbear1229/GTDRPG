package com.questlog.feature.encounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.EncounterLog
import com.questlog.core.domain.model.EncounterStatus
import com.questlog.core.domain.repository.EncounterRepository
import com.questlog.core.domain.usecase.ClaimEncounterRewardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EncounterUiState(
    val pending: List<EncounterLog> = emptyList(),
    val history: List<EncounterLog> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed class EncounterEvent {
    data class Claimed(val xpGained: Long, val newLevel: Int) : EncounterEvent()
    data class Error(val message: String) : EncounterEvent()
}

@HiltViewModel
class EncounterViewModel @Inject constructor(
    private val encounterRepository: EncounterRepository,
    private val claimEncounterReward: ClaimEncounterRewardUseCase,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _events = MutableSharedFlow<EncounterEvent>()
    val events: SharedFlow<EncounterEvent> = _events

    val uiState: StateFlow<EncounterUiState> = combine(
        encounterRepository.getPending(),
        encounterRepository.getAll(),
        _error,
    ) { pending, all, error ->
        EncounterUiState(
            pending = pending,
            history = all.filter { it.status != EncounterStatus.PENDING },
            isLoading = false,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EncounterUiState(),
    )

    fun claimReward(encounterId: String) {
        viewModelScope.launch {
            when (val result = claimEncounterReward(encounterId)) {
                is ClaimEncounterRewardUseCase.Result.Success ->
                    _events.emit(EncounterEvent.Claimed(result.xpGained, result.newLevel))
                is ClaimEncounterRewardUseCase.Result.AlreadyClaimed ->
                    _error.update { "이미 처리된 인카운터입니다." }
                is ClaimEncounterRewardUseCase.Result.NotFound ->
                    _error.update { "인카운터를 찾을 수 없습니다." }
                is ClaimEncounterRewardUseCase.Result.NoCharacter ->
                    _error.update { "활성 캐릭터가 없습니다." }
            }
        }
    }

    fun dismissError() = _error.update { null }
}
