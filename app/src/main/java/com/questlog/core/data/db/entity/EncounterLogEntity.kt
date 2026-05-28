package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "encounter_logs",
    indices = [
        Index(value = ["status", "expiresAt"]),
        Index("generatedAt"),
    ],
)
data class EncounterLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateKey: String,
    val status: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val claimedAt: Long? = null,
    val expiresAt: Long,
    val rewardXp: Long,
    val rewardItemId: String? = null,
)
