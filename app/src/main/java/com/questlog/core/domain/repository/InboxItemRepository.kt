package com.questlog.core.domain.repository

import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.InboxItem
import kotlinx.coroutines.flow.Flow

interface InboxItemRepository {
    fun observeUnclarified(): Flow<List<InboxItem>>
    fun observeUnclarifiedCount(): Flow<Int>
    suspend fun capture(rawText: String, source: CaptureSource): String
    suspend fun getById(id: String): InboxItem?
    suspend fun update(item: InboxItem)
    suspend fun delete(id: String)
}
