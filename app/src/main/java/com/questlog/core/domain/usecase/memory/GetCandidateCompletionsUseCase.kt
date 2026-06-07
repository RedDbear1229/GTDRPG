package com.questlog.core.domain.usecase.memory

import com.questlog.core.domain.model.TaskSummary
import com.questlog.core.domain.repository.CompletionRepository
import javax.inject.Inject

/**
 * 특정 날짜에 완료된 Task의 TaskSummary 목록을 반환한다.
 * CombatLog의 d20Result 기준 OutcomeType 은 CompletionRepository 에서 결정.
 *   - d20 = 20 → STRONG_HIT
 *   - d20 = 1  → MISS
 *   - d20 >= 2 → WEAK_HIT
 *   - 로그 없음 (QuickDone/2분룰) → NONE
 */
class GetCandidateCompletionsUseCase @Inject constructor(
    private val completionRepository: CompletionRepository,
) {
    suspend operator fun invoke(date: String): List<TaskSummary> =
        completionRepository.getCompletedTaskSummariesByDate(date)
}
