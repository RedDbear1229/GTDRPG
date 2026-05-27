package com.questlog.core.domain.usecase

import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.domain.model.Npc
import com.questlog.core.domain.model.NpcSource
import com.questlog.core.domain.model.TaskStatus
import com.questlog.core.domain.repository.TaskRepository
import javax.inject.Inject

class DelegateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val consentManager: ConsentManager,
) {
    sealed class Result {
        data class Success(val taskId: String, val npcId: String) : Result()
        object ContactsConsentRequired : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(taskId: String, npc: Npc): Result {
        if (npc.source == NpcSource.PICKER && !consentManager.canImportContacts()) {
            return Result.ContactsConsentRequired
        }
        val task = taskRepository.getById(taskId) ?: return Result.Error("퀘스트를 찾을 수 없습니다")
        val now = System.currentTimeMillis()
        taskRepository.upsert(
            task.copy(
                status = TaskStatus.WAITING,
                delegatedTo = npc.id,
                delegatedAt = now,
                updatedAt = now,
            )
        )
        return Result.Success(taskId, npc.id)
    }
}
