package com.questlog.core.data.mapper

import com.questlog.core.data.db.entity.InboxItemEntity
import com.questlog.core.domain.model.InboxItem

fun InboxItemEntity.toDomain() = InboxItem(
    id = id,
    rawText = rawText,
    audioPath = audioPath,
    transcribedText = transcribedText,
    imagePaths = imagePaths,
    capturedAt = capturedAt,
    source = source,
    isClarified = isClarified,
    clarifiedAt = clarifiedAt,
    resultType = resultType,
    clarifiedTaskId = clarifiedTaskId,
    clarifiedProjectId = clarifiedProjectId,
)

fun InboxItem.toEntity() = InboxItemEntity(
    id = id,
    rawText = rawText,
    audioPath = audioPath,
    transcribedText = transcribedText,
    imagePaths = imagePaths,
    capturedAt = capturedAt,
    source = source,
    isClarified = isClarified,
    clarifiedAt = clarifiedAt,
    resultType = resultType,
    clarifiedTaskId = clarifiedTaskId,
    clarifiedProjectId = clarifiedProjectId,
)
