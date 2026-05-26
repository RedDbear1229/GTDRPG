package com.questlog.core.domain.repository

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatLog

interface CompletionRepository {
    // false = status guard 가 0 rows 반환 → 이미 완료 (AlreadyCompleted 신호)
    suspend fun completeTask(
        taskId: String,
        log: CombatLog,
        updatedCharacter: Character,
        now: Long,
    ): Boolean
}
