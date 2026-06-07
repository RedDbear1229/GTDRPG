package com.questlog.core.domain.usecase.memory

import com.questlog.core.domain.model.MemoryEntry
import com.questlog.core.domain.model.TaskSummary
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.InsertResult
import com.questlog.core.domain.repository.MemoryRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

private const val BODY_MAX_LENGTH = 500

sealed class SaveResult {
    object Success : SaveResult()
    object AlreadyExists : SaveResult()
    object NoCharacter : SaveResult()
}

/**
 * 오늘의 기억을 저장한다.
 *
 * - body 500자 초과 시 truncate (손실 최소화)
 * - date 는 invoke() 시점에 캐시 → 자정 경계 race 방지
 * - sealedAt = 다음날 00:00 로컬 epoch ms
 * - characterId = 활성 캐릭터 id (없으면 NoCharacter)
 */
class SaveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(taskSummary: TaskSummary, body: String): SaveResult {
        // 날짜를 저장 시작 시점에 캐시 (자정 race 방지)
        val today = LocalDate.now()
        val dateString = today.toString()

        val character = characterRepository.getActive() ?: return SaveResult.NoCharacter

        val truncatedBody = body.take(BODY_MAX_LENGTH)

        // 다음날 00:00 로컬 epoch ms
        val zone = ZoneId.systemDefault()
        val sealedAt = today.plusDays(1)
            .atTime(LocalTime.MIDNIGHT)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val entry = MemoryEntry(
            id = UUID.randomUUID().toString(),
            entryDate = dateString,
            characterId = character.id,
            taskId = taskSummary.id,
            taskTitleSnapshot = taskSummary.title,
            outcomeType = taskSummary.outcomeType,
            body = truncatedBody,
            enrichedBody = null,
            createdAt = System.currentTimeMillis(),
            sealedAt = sealedAt,
        )

        return when (memoryRepository.insertEntry(entry)) {
            is InsertResult.Success -> SaveResult.Success
            is InsertResult.AlreadyExists -> SaveResult.AlreadyExists
        }
    }
}
