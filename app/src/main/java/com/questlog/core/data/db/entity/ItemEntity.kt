package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.ItemRarity
import com.questlog.core.domain.model.ItemType
import java.util.UUID

// 아이템 인스턴스. itemKey 로 ItemCatalog.ItemTemplate 을 참조.
// characterId 없음 — 소유 관계는 character_items (CharacterItemEntity) 로 관리.
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemKey: String,
    val name: String,
    val description: String,
    val flavorText: String? = null,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val slot: EquipmentSlot,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val xpMultiplier: Float = 1.0f,
    val hpBonusFlat: Int = 0,
    val specialEffectCode: String? = null,
)
