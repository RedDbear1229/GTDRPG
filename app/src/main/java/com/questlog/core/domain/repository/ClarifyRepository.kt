package com.questlog.core.domain.repository

import com.questlog.core.domain.model.ClarifyResultType
import com.questlog.core.domain.model.Task

// 명료화 결과 단일 트랜잭션으로 커밋. InboxItem 갱신 + (옵션) Task 생성을 원자적으로 처리.
// docs/03_gtd_system.md §3.3 결정 트리.
interface ClarifyRepository {
    suspend fun finalize(inboxId: String, outcome: ClarifyOutcome)
}

sealed class ClarifyOutcome {
    abstract val resultType: ClarifyResultType

    /** Q1 → "버릴 것". InboxItem.isClarified = true, resultType = DELETED, no Task created. */
    data object Discard : ClarifyOutcome() {
        override val resultType = ClarifyResultType.DELETED
    }

    /** TASK / SOMEDAY / REFERENCE / DONE_NOW — Task 엔티티를 새로 생성. */
    data class StoreAsTask(
        val task: Task,
        override val resultType: ClarifyResultType,
    ) : ClarifyOutcome()
}
