package com.questlog.core.domain.usecase

import com.questlog.core.domain.repository.ConsentRepository
import com.questlog.core.domain.model.ConsentScope
import javax.inject.Inject

class DeleteAiCacheUseCase @Inject constructor(
    private val consentRepository: ConsentRepository,
) {
    suspend operator fun invoke() {
        // AI 동의 철회 — 이후 canCallApi() = false
        consentRepository.revoke(ConsentScope.AI_OUTBOUND)
    }
}
