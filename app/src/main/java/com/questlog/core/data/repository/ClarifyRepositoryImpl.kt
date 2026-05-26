package com.questlog.core.data.repository

import androidx.room.withTransaction
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.repository.ClarifyOutcome
import com.questlog.core.domain.repository.ClarifyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClarifyRepositoryImpl @Inject constructor(
    private val database: QuestLogDatabase,
    private val inboxDao: InboxItemDao,
    private val taskDao: TaskDao,
) : ClarifyRepository {

    override suspend fun finalize(inboxId: String, outcome: ClarifyOutcome) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val existing = inboxDao.getById(inboxId)
                    ?: error("InboxItem $inboxId not found")
                // Idempotency: 이미 명료화된 항목은 중복 처리 차단 (재진입/리트라이 시 Task 중복 INSERT 방지)
                if (existing.isClarified) return@withTransaction
                val now = System.currentTimeMillis()
                val createdTaskId = (outcome as? ClarifyOutcome.StoreAsTask)?.task?.id
                val createdProjectId = (outcome as? ClarifyOutcome.StoreAsTask)?.task?.projectId
                val updated = existing.copy(
                    isClarified = true,
                    clarifiedAt = now,
                    resultType = outcome.resultType,
                    clarifiedTaskId = createdTaskId,
                    clarifiedProjectId = createdProjectId,
                )
                if (outcome is ClarifyOutcome.StoreAsTask) {
                    taskDao.insert(outcome.task.copy(inboxItemId = existing.id).toEntity())
                }
                inboxDao.update(updated)
            }
        }
    }
}
