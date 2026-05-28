package com.questlog.core.data.mapper

import com.questlog.core.data.EncounterTemplates
import com.questlog.core.data.db.entity.EncounterLogEntity
import com.questlog.core.domain.model.EncounterLog
import com.questlog.core.domain.model.EncounterStatus

fun EncounterLogEntity.toDomain(): EncounterLog {
    val template = EncounterTemplates.forKey(templateKey)
    return EncounterLog(
        id = id,
        templateKey = templateKey,
        title = template?.title ?: templateKey,
        description = template?.description ?: "",
        flavorText = template?.flavorText ?: "",
        status = EncounterStatus.valueOf(status),
        generatedAt = generatedAt,
        claimedAt = claimedAt,
        expiresAt = expiresAt,
        rewardXp = rewardXp,
        rewardItemId = rewardItemId,
    )
}

fun EncounterLog.toEntity() = EncounterLogEntity(
    id = id,
    templateKey = templateKey,
    status = status.name,
    generatedAt = generatedAt,
    claimedAt = claimedAt,
    expiresAt = expiresAt,
    rewardXp = rewardXp,
    rewardItemId = rewardItemId,
)
