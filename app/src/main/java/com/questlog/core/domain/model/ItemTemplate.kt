package com.questlog.core.domain.model

// 아이템 카탈로그의 정적 정의. ItemEntity 생성 시 이 데이터를 복사.
data class ItemTemplate(
    val key: String,
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
