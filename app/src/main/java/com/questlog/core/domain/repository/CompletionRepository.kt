package com.questlog.core.domain.repository

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatLog
import com.questlog.core.domain.model.TaskSummary

// android.* import 금지 — KMP 확장 호환성 유지.

interface CompletionRepository {
    // false = status guard 가 0 rows 반환 → 이미 완료 (AlreadyCompleted 신호)
    suspend fun completeTask(
        taskId: String,
        log: CombatLog,
        updatedCharacter: Character,
        now: Long,
    ): Boolean

    // F6.1 Memory of the Day: 특정 날짜에 완료된 퀘스트 + 전투 결과
    suspend fun getCompletedTaskSummariesByDate(date: String): List<TaskSummary>
}
