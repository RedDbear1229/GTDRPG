package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.ClarifyResultType
import java.util.UUID

@Entity(tableName = "inbox_items")
data class InboxItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val rawText: String,
    val audioPath: String? = null,
    val transcribedText: String? = null,
    val imagePaths: List<String> = emptyList(),
    val capturedAt: Long = System.currentTimeMillis(),
    val source: CaptureSource,
    val isClarified: Boolean = false,
    val clarifiedAt: Long? = null,
    val resultType: ClarifyResultType? = null,
    val clarifiedTaskId: String? = null,
    val clarifiedProjectId: String? = null,
)
