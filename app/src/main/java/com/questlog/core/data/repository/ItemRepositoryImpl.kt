package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.CharacterItemDao
import com.questlog.core.data.db.entity.CharacterItemEntity
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.model.ItemTemplate
import com.questlog.core.domain.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val dao: CharacterItemDao,
) : ItemRepository {

    override suspend fun addDroppedItem(
        template: ItemTemplate,
        characterId: String,
        acquiredFromTaskId: String?,
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val itemEntity = template.toEntity(id)
        val characterItem = CharacterItemEntity(
            characterId = characterId,
            itemId = id,
            acquiredAt = System.currentTimeMillis(),
            acquiredFromTaskId = acquiredFromTaskId,
            updatedAt = System.currentTimeMillis(),
        )
        dao.addDroppedItem(itemEntity, characterItem)
    }

    override fun getEquippedItems(characterId: String): Flow<List<Item>> =
        dao.getEquippedItems(characterId).map { list -> list.map { it.toDomain() } }

    override fun getInventory(characterId: String): Flow<List<Item>> =
        dao.getInventory(characterId).map { list -> list.map { it.toDomain() } }

    override suspend fun equipItem(characterId: String, itemId: String, slot: EquipmentSlot) =
        withContext(Dispatchers.IO) { dao.equipItem(characterId, itemId, slot) }

    override suspend fun unequipItem(characterId: String, itemId: String) =
        withContext(Dispatchers.IO) { dao.unequipItem(characterId, itemId) }

    override suspend fun removeFromInventory(characterId: String, itemId: String) =
        withContext(Dispatchers.IO) { dao.removeFromInventory(characterId, itemId) }
}
