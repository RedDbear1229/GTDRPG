package com.questlog.core.data.mapper

import com.questlog.core.data.db.entity.NpcEntity
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.Npc
import com.questlog.core.domain.model.NpcSource

fun NpcEntity.toDomain() = Npc(
    id = id,
    name = name,
    displayName = displayName,
    phoneNumber = phoneNumber,
    classType = CharacterClass.valueOf(classType),
    source = NpcSource.valueOf(source),
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Npc.toEntity() = NpcEntity(
    id = id,
    name = name,
    displayName = displayName,
    phoneNumber = phoneNumber,
    classType = classType.name,
    source = source.name,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
