package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.questlog.core.data.db.entity.CharacterItemEntity
import com.questlog.core.data.db.entity.ItemEntity
import com.questlog.core.domain.model.EquipmentSlot
import kotlinx.coroutines.flow.Flow

// character_items + items JOIN 결과. @Relation 으로 Room 이 N+1 쿼리 처리.
data class CharacterItemWithDetail(
    @Embedded val characterItem: CharacterItemEntity,
    @Relation(parentColumn = "itemId", entityColumn = "id")
    val item: ItemEntity,
)

@Dao
interface CharacterItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCharacterItem(item: CharacterItemEntity): Long

    // 아이템 드롭 — items + character_items 동시 INSERT (단일 트랜잭션)
    @Transaction
    suspend fun addDroppedItem(item: ItemEntity, characterItem: CharacterItemEntity): String {
        insertItem(item)
        insertCharacterItem(characterItem)
        return item.id
    }

    // equippedSlot 은 TypeConverter 로 String 변환 후 저장
    @Query("""
        UPDATE character_items
        SET isEquipped = :equipped, equippedSlot = :slot, updatedAt = :now
        WHERE characterId = :characterId AND itemId = :itemId
    """)
    suspend fun setEquipped(
        characterId: String, itemId: String,
        equipped: Boolean, slot: EquipmentSlot?,
        now: Long = System.currentTimeMillis(),
    )

    // slotName = EquipmentSlot.name 문자열 (equipItem 에서 변환해 전달)
    @Query("""
        UPDATE character_items
        SET isEquipped = 0, equippedSlot = NULL, updatedAt = :now
        WHERE characterId = :characterId AND equippedSlot = :slotName
    """)
    suspend fun unequipSlot(
        characterId: String, slotName: String,
        now: Long = System.currentTimeMillis(),
    )

    // 슬롯 기존 장비 해제 후 신규 장착 (슬롯 단일성 보장)
    @Transaction
    suspend fun equipItem(characterId: String, itemId: String, slot: EquipmentSlot) {
        unequipSlot(characterId, slot.name)
        setEquipped(characterId, itemId, true, slot)
    }

    @Transaction
    suspend fun unequipItem(characterId: String, itemId: String) {
        setEquipped(characterId, itemId, false, null)
    }

    @Transaction
    @Query("SELECT * FROM character_items WHERE characterId = :characterId AND isEquipped = 1")
    fun getEquippedItems(characterId: String): Flow<List<CharacterItemWithDetail>>

    @Transaction
    @Query("SELECT * FROM character_items WHERE characterId = :characterId ORDER BY acquiredAt DESC")
    fun getInventory(characterId: String): Flow<List<CharacterItemWithDetail>>

    @Query("SELECT COUNT(*) FROM character_items WHERE characterId = :characterId")
    fun getInventoryCount(characterId: String): Flow<Int>

    @Query("DELETE FROM character_items WHERE characterId = :characterId AND itemId = :itemId")
    suspend fun removeFromInventory(characterId: String, itemId: String)
}
