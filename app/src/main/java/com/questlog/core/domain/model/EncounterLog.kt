package com.questlog.core.domain.model

import java.util.UUID

data class EncounterLog(
    val id: String = UUID.randomUUID().toString(),
    val templateKey: String,
    val title: String,
    val description: String,
    val flavorText: String,
    val status: EncounterStatus,
    val generatedAt: Long,
    val claimedAt: Long? = null,
    val expiresAt: Long,
    val rewardXp: Long,
    val rewardItemId: String? = null,
) {
    val isExpired: Boolean get() = status == EncounterStatus.EXPIRED ||
        (status == EncounterStatus.PENDING && System.currentTimeMillis() > expiresAt)
}
