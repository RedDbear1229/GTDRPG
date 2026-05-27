package com.questlog.core.domain.repository

import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.model.ItemTemplate
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    suspend fun addDroppedItem(
        template: ItemTemplate,
        characterId: String,
        acquiredFromTaskId: String? = null,
    ): String

    fun getEquippedItems(characterId: String): Flow<List<Item>>
    fun getInventory(characterId: String): Flow<List<Item>>

    suspend fun equipItem(characterId: String, itemId: String, slot: EquipmentSlot)
    suspend fun unequipItem(characterId: String, itemId: String)
    suspend fun removeFromInventory(characterId: String, itemId: String)
}
