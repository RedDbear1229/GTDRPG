package com.questlog.core.domain.model

// CharacterItemWithDetail 의 DB 표현을 변환한 도메인 모델.
data class Item(
    val id: String,
    val itemKey: String,
    val name: String,
    val description: String,
    val flavorText: String?,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val slot: EquipmentSlot,
    val attackBonus: Int,
    val defenseBonus: Int,
    val xpMultiplier: Float,
    val hpBonusFlat: Int,
    val specialEffectCode: String?,
    val isEquipped: Boolean,
    val equippedSlot: EquipmentSlot?,
    val acquiredAt: Long,
    val characterId: String,
    val acquiredFromTaskId: String?,
)
