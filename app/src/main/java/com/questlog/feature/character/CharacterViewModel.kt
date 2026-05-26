package com.questlog.feature.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.usecase.CheckLevelUpUseCase
import com.questlog.core.domain.usecase.GainXPUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterUiState(
    val character: Character? = null,
    val isLoading: Boolean = true,
    val levelUpEvent: Boolean = false,  // true 이면 LevelUpScreen 표시 (1회 소비)
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val gainXPUseCase: GainXPUseCase,
) : ViewModel() {

    private val _levelUpEvent = MutableStateFlow(false)

    val uiState: StateFlow<CharacterUiState> = kotlinx.coroutines.flow.combine(
        characterRepository.observeActive(),
        _levelUpEvent,
    ) { character, levelUp ->
        CharacterUiState(character = character, isLoading = false, levelUpEvent = levelUp)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CharacterUiState())

    fun gainXp(amount: Long) {
        viewModelScope.launch {
            val before = characterRepository.getActive() ?: return@launch
            val after = gainXPUseCase(amount) ?: return@launch
            if (CheckLevelUpUseCase.didLevelUp(before, after)) {
                _levelUpEvent.value = true
            }
        }
    }

    // LevelUpScreen 이 표시된 후 소비 처리
    fun consumeLevelUpEvent() = _levelUpEvent.update { false }
}
