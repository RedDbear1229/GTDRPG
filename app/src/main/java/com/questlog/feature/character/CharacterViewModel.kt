package com.questlog.feature.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.ItemRepository
import com.questlog.core.domain.usecase.CheckLevelUpUseCase
import com.questlog.core.domain.usecase.EquipItemUseCase
import com.questlog.core.domain.usecase.GainXPUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterUiState(
    val character: Character? = null,
    val isLoading: Boolean = true,
    val levelUpEvent: Boolean = false,
    val equippedItems: List<Item> = emptyList(),
    val inventory: List<Item> = emptyList(),
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val itemRepository: ItemRepository,
    private val gainXPUseCase: GainXPUseCase,
    private val equipItemUseCase: EquipItemUseCase,
) : ViewModel() {

    private val _levelUpEvent = MutableStateFlow(false)

    private val activeCharacterFlow = characterRepository.observeActive()

    // 캐릭터 ID 변화 시 인벤토리·장착 Flow 갱신 (flatMapLatest)
    private val equippedFlow = activeCharacterFlow.flatMapLatest { char ->
        if (char == null) flowOf(emptyList()) else itemRepository.getEquippedItems(char.id)
    }
    private val inventoryFlow = activeCharacterFlow.flatMapLatest { char ->
        if (char == null) flowOf(emptyList()) else itemRepository.getInventory(char.id)
    }

    val uiState: StateFlow<CharacterUiState> = combine(
        activeCharacterFlow,
        _levelUpEvent,
        equippedFlow,
        inventoryFlow,
    ) { character, levelUp, equipped, inventory ->
        CharacterUiState(
            character = character,
            isLoading = false,
            levelUpEvent = levelUp,
            equippedItems = equipped,
            inventory = inventory,
        )
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

    fun equipItem(itemId: String, slot: EquipmentSlot) {
        viewModelScope.launch {
            val characterId = characterRepository.getActive()?.id ?: return@launch
            equipItemUseCase(characterId, itemId, slot)
        }
    }

    fun unequipItem(itemId: String) {
        viewModelScope.launch {
            val characterId = characterRepository.getActive()?.id ?: return@launch
            itemRepository.unequipItem(characterId, itemId)
        }
    }

    fun consumeLevelUpEvent() = _levelUpEvent.update { false }
}
