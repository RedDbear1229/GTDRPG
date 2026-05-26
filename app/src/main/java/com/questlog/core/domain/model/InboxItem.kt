package com.questlog.core.domain.model

import java.util.UUID

data class InboxItem(
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
