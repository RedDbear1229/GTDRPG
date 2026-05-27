package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "npcs",
    indices = [Index("source")],
)
data class NpcEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val classType: String,
    val source: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
