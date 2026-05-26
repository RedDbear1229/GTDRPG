package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.LifeArea
import javax.inject.Inject

// 2분 룰 진입점. F1.3 단계에선 ClarifyItemUseCase 에 위임하여 status=DONE + isQuickDone=true 로 저장.
// 전투 로그/XP 적립은 F3.1 CompletionDao 가 인계받기 전까지 보류.
class QuickDoneUseCase @Inject constructor(
    private val clarifyItem: ClarifyItemUseCase,
) {
    suspend operator fun invoke(
        inboxId: String,
        nextAction: String?,
        lifeArea: LifeArea = LifeArea.PERSONAL,
    ): ClarifyResult = clarifyItem(
        ClarifyDraft(
            inboxId = inboxId,
            isActionable = true,
            nextAction = nextAction,
            isTwoMinute = true,
            estimatedMinutes = 2,
            lifeArea = lifeArea,
        ),
    )
}
