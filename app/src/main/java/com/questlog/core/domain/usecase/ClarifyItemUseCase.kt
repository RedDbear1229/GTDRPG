package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.ClarifyResultType
import com.questlog.core.domain.model.InboxItem
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import com.questlog.core.domain.repository.ClarifyOutcome
import com.questlog.core.domain.repository.ClarifyRepository
import com.questlog.core.domain.repository.InboxItemRepository
import com.questlog.core.domain.util.ContextExtractor
import com.questlog.core.domain.util.MonsterTypeMapper
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

// docs/03_gtd_system.md §3.3 결정 트리 Q1~Q6 결과를 Task/Discard 로 정규화.
// Q4 "내가 해야 하나? → 아니오" 위임 경로는 status=WAITING 만 부여; NPC 선택 UI 는 Phase 2 (F2.x) 도입.
class ClarifyItemUseCase @Inject constructor(
    private val inboxRepository: InboxItemRepository,
    private val clarifyRepository: ClarifyRepository,
) {

    suspend operator fun invoke(draft: ClarifyDraft): ClarifyResult {
        val inboxItem = inboxRepository.getById(draft.inboxId)
            ?: return ClarifyResult.Error("Inbox 항목을 찾을 수 없습니다")
        val outcome = buildOutcome(inboxItem, draft)
        return runCatching { clarifyRepository.finalize(draft.inboxId, outcome) }
            .map {
                when (outcome) {
                    is ClarifyOutcome.Discard -> ClarifyResult.Discarded
                    is ClarifyOutcome.StoreAsTask -> ClarifyResult.Stored(outcome.task.id)
                }
            }
            .getOrElse { e ->
                Timber.e(e, "clarify finalize failed inbox=${draft.inboxId}")
                ClarifyResult.Error(e.message ?: "알 수 없는 오류")
            }
    }

    private fun buildOutcome(inboxItem: InboxItem, draft: ClarifyDraft): ClarifyOutcome {
        if (!draft.isActionable) {
            return when (draft.nonActionable) {
                NonActionable.DELETE -> ClarifyOutcome.Discard
                NonActionable.SOMEDAY -> ClarifyOutcome.StoreAsTask(
                    task = buildTask(inboxItem, draft, TaskStatus.SOMEDAY),
                    resultType = ClarifyResultType.SOMEDAY,
                )
                NonActionable.REFERENCE -> ClarifyOutcome.StoreAsTask(
                    task = buildTask(inboxItem, draft, TaskStatus.REFERENCE),
                    resultType = ClarifyResultType.REFERENCE,
                )
                null -> ClarifyOutcome.Discard
            }
        }
        // actionable
        val status = when {
            draft.isTwoMinute -> TaskStatus.DONE
            draft.delegate -> TaskStatus.WAITING
            else -> TaskStatus.ACTIVE
        }
        val resultType = if (draft.isTwoMinute) ClarifyResultType.DONE_NOW else ClarifyResultType.TASK
        return ClarifyOutcome.StoreAsTask(
            task = buildTask(inboxItem, draft, status),
            resultType = resultType,
        )
    }

    private fun buildTask(inboxItem: InboxItem, draft: ClarifyDraft, status: TaskStatus): Task {
        val cr = CrCalculator.calculate(
            estimatedMinutes = draft.estimatedMinutes ?: 0,
            hasDeadline = draft.dueDate != null,
            daysUntilDeadline = draft.dueDate?.let { daysUntil(it) },
            complexitySteps = draft.complexitySteps,
            lifeArea = draft.lifeArea,
            isRecurring = draft.isRecurring,
        )
        val now = System.currentTimeMillis()
        val isDone = status == TaskStatus.DONE
        val contextTags = ContextExtractor.extract(inboxItem.rawText)
            .plus(draft.contextTags)
            .distinct()
        return Task(
            id = UUID.randomUUID().toString(),
            title = inboxItem.rawText.lineSequence().firstOrNull()?.trim().orEmpty()
                .ifBlank { draft.nextAction.orEmpty() },
            description = inboxItem.rawText.takeIf { it.lines().size > 1 },
            nextAction = draft.nextAction?.takeIf { it.isNotBlank() },
            projectId = draft.projectId,
            status = status,
            lifeArea = draft.lifeArea,
            context = ContextExtractor.joinForStorage(contextTags),
            primaryAbility = draft.lifeArea.primaryAbility,
            challengeRating = cr,
            monsterType = MonsterTypeMapper.fromCr(cr),
            estimatedMinutes = draft.estimatedMinutes,
            dueDate = draft.dueDate,
            delegatedAt = if (status == TaskStatus.WAITING) now else null,
            isQuickDone = draft.isTwoMinute,
            inboxItemId = inboxItem.id,
            createdAt = now,
            completedAt = if (isDone) now else null,
            updatedAt = now,
        )
    }

    private fun daysUntil(epochMillis: Long, now: Long = System.currentTimeMillis()): Int {
        val diff = epochMillis - now
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
}

data class ClarifyDraft(
    val inboxId: String,
    val isActionable: Boolean,
    val nonActionable: NonActionable? = null,
    val nextAction: String? = null,
    val isTwoMinute: Boolean = false,
    val delegate: Boolean = false,
    val projectId: String? = null,
    val estimatedMinutes: Int? = null,
    val dueDate: Long? = null,
    val lifeArea: LifeArea = LifeArea.PERSONAL,
    val contextTags: List<String> = emptyList(),
    val complexitySteps: Int = 1,
    val isRecurring: Boolean = false,
)

enum class NonActionable { DELETE, SOMEDAY, REFERENCE }

sealed class ClarifyResult {
    data class Stored(val taskId: String) : ClarifyResult()
    data object Discarded : ClarifyResult()
    data class Error(val message: String) : ClarifyResult()
}
