package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatLog
import com.questlog.core.domain.repository.CompletionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompletionRepositoryImpl @Inject constructor(
    private val dao: CompletionDao,
) : CompletionRepository {

    override suspend fun completeTask(
        taskId: String,
        log: CombatLog,
        updatedCharacter: Character,
        now: Long,
    ): Boolean = withContext(Dispatchers.IO) {
        dao.commitCompletion(
            taskId = taskId,
            log = log.toEntity(),
            updatedCharacter = updatedCharacter.toEntity(),
            now = now,
        )
    }
}
