package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.repository.ItemRepository
import javax.inject.Inject

class EquipItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(characterId: String, itemId: String, slot: EquipmentSlot) =
        itemRepository.equipItem(characterId, itemId, slot)
}
