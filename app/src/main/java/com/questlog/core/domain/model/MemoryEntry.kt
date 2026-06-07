package com.questlog.core.domain.model


enum class OutcomeType {
    STRONG_HIT,  // D20 = 20 (크리티컬)
    WEAK_HIT,    // D20 >= 2 (일반 성공)
    MISS,        // D20 = 1 (크리티컬 미스)
    NONE,        // 전투 없이 완료 (2분 룰 등)
}

data class MemoryEntry(
    val id: String,
    val entryDate: String,           // "yyyy-MM-dd"
    val characterId: String,
    val taskId: String?,
    val taskTitleSnapshot: String,
    val outcomeType: OutcomeType,
    val body: String,
    val enrichedBody: String?,
    val createdAt: Long,
    val sealedAt: Long,
)

// 오늘의 기억 작성 단계 — UI 가 이 상태를 구독
sealed class MemoryTodayState {
    /** 오늘 완료한 퀘스트가 없어 기억을 남길 수 없음 */
    object NoCompletions : MemoryTodayState()

    /** 오늘 완료 퀘스트가 있어 사용자가 퀘스트를 선택 중 */
    data class Selecting(val candidates: List<TaskSummary>) : MemoryTodayState()

    /** 퀘스트를 선택했고 본문을 작성 중 */
    data class Writing(val selected: TaskSummary) : MemoryTodayState()

    /** 저장 완료 — 읽기 전용 */
    data class Saved(val entry: MemoryEntry) : MemoryTodayState()

    /** 어제 작성하지 않아 어제 메모가 잠김 (미작성은 영구 만료) */
    data class Expired(val date: String) : MemoryTodayState()
}

/** 완료한 퀘스트의 요약 — MemoryTodayScreen 후보 목록에 표시 */
data class TaskSummary(
    val id: String,
    val title: String,
    val outcomeType: OutcomeType,
    val xpGained: Long,
)
