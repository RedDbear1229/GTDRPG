package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.questlog.core.domain.model.EquipmentSlot

// character_items: 캐릭터-아이템 소유·장착 상태 junction 테이블.
// FK CASCADE: 캐릭터 삭제 시 인벤토리 전체 삭제, 아이템 삭제 시 junction 행 삭제.
// 슬롯 단일성: DAO @Transaction equipItem() + DB UNIQUE(characterId, equippedSlot).
// 미장착 아이템은 항상 equippedSlot=NULL → SQLite UNIQUE는 NULL 중복 허용으로 안전.
@Entity(
    tableName = "character_items",
    primaryKeys = ["characterId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("characterId"),
        Index("itemId"),
        Index("equippedSlot"),
        Index(
            value = ["characterId", "equippedSlot"],
            unique = true,
            name = "index_character_items_equipped_slot_unique",
        ),
    ],
)
data class CharacterItemEntity(
    val characterId: String,
    val itemId: String,
    val isEquipped: Boolean = false,
    val equippedSlot: EquipmentSlot? = null,
    val acquiredAt: Long = System.currentTimeMillis(),
    val acquiredFromTaskId: String? = null,
    val acquiredFromEncounterId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)
