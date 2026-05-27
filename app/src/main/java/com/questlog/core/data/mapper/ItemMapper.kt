package com.questlog.core.data.mapper

import com.questlog.core.data.db.dao.CharacterItemWithDetail
import com.questlog.core.data.db.entity.CharacterItemEntity
import com.questlog.core.data.db.entity.ItemEntity
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.model.ItemTemplate

fun CharacterItemWithDetail.toDomain() = Item(
    id = item.id,
    itemKey = item.itemKey,
    name = item.name,
    description = item.description,
    flavorText = item.flavorText,
    itemType = item.itemType,
    rarity = item.rarity,
    slot = item.slot,
    attackBonus = item.attackBonus,
    defenseBonus = item.defenseBonus,
    xpMultiplier = item.xpMultiplier,
    hpBonusFlat = item.hpBonusFlat,
    specialEffectCode = item.specialEffectCode,
    isEquipped = characterItem.isEquipped,
    equippedSlot = characterItem.equippedSlot,
    acquiredAt = characterItem.acquiredAt,
    characterId = characterItem.characterId,
    acquiredFromTaskId = characterItem.acquiredFromTaskId,
)

fun ItemTemplate.toEntity(id: String) = ItemEntity(
    id = id,
    itemKey = key,
    name = name,
    description = description,
    flavorText = flavorText,
    itemType = itemType,
    rarity = rarity,
    slot = slot,
    attackBonus = attackBonus,
    defenseBonus = defenseBonus,
    xpMultiplier = xpMultiplier,
    hpBonusFlat = hpBonusFlat,
    specialEffectCode = specialEffectCode,
)

fun CharacterItemEntity.toDomain(item: ItemEntity) = CharacterItemWithDetail(
    characterItem = this,
    item = item,
).toDomain()
