package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.InboxItem
import com.questlog.core.domain.repository.InboxItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxItemRepositoryImpl @Inject constructor(
    private val dao: InboxItemDao,
) : InboxItemRepository {

    override fun observeUnclarified(): Flow<List<InboxItem>> =
        dao.getUnclarified().map { list -> list.map { it.toDomain() } }

    override fun observeUnclarifiedCount(): Flow<Int> = dao.getUnclarifiedCount()

    override suspend fun capture(rawText: String, source: CaptureSource): String =
        withContext(Dispatchers.IO) {
            val item = InboxItem(rawText = rawText, source = source)
            dao.insert(item.toEntity())
            item.id
        }

    override suspend fun getById(id: String): InboxItem? =
        withContext(Dispatchers.IO) { dao.getById(id)?.toDomain() }

    override suspend fun update(item: InboxItem) =
        withContext(Dispatchers.IO) { dao.update(item.toEntity()) }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) { dao.delete(id) }

    override suspend fun clearVoiceTranscripts(): Int =
        withContext(Dispatchers.IO) { dao.clearVoiceTranscripts() }
}
