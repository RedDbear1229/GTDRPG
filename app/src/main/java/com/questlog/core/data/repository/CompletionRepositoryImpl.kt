package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatLog
import com.questlog.core.domain.model.OutcomeType
import com.questlog.core.domain.model.TaskSummary
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

    override suspend fun getCompletedTaskSummariesByDate(date: String): List<TaskSummary> =
        withContext(Dispatchers.IO) {
            dao.getCompletedWithLogByDate(date).map { item ->
                val outcome = when (item.d20Result) {
                    null -> OutcomeType.NONE
                    20 -> OutcomeType.STRONG_HIT
                    1 -> OutcomeType.MISS
                    else -> OutcomeType.WEAK_HIT
                }
                TaskSummary(
                    id = item.taskId,
                    title = item.taskTitle,
                    outcomeType = outcome,
                    xpGained = item.xpGained,
                )
            }
        }
}
