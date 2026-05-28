package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "consent_records",
    indices = [Index("scope")],
)
data class ConsentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scope: String,
    val policyVersion: Int,
    val acceptedAt: Long,
    val revokedAt: Long? = null,
)
