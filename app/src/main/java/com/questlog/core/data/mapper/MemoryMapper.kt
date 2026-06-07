package com.questlog.core.data.mapper

import com.questlog.core.data.db.entity.MemoryEntryEntity
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.core.domain.model.OutcomeType

fun MemoryEntryEntity.toDomain(): MemoryEntry = MemoryEntry(
    id = id,
    entryDate = entryDate,
    characterId = characterId,
    taskId = taskId,
    taskTitleSnapshot = taskTitleSnapshot,
    outcomeType = outcomeType.toOutcomeType(),
    body = body,
    enrichedBody = enrichedBody,
    createdAt = createdAt,
    sealedAt = sealedAt,
)

fun MemoryEntry.toEntity(): MemoryEntryEntity = MemoryEntryEntity(
    id = id,
    entryDate = entryDate,
    characterId = characterId,
    taskId = taskId,
    taskTitleSnapshot = taskTitleSnapshot,
    outcomeType = outcomeType.name,
    body = body,
    enrichedBody = enrichedBody,
    createdAt = createdAt,
    sealedAt = sealedAt,
)

private fun String.toOutcomeType(): OutcomeType =
    runCatching { OutcomeType.valueOf(this) }.getOrElse { OutcomeType.NONE }
