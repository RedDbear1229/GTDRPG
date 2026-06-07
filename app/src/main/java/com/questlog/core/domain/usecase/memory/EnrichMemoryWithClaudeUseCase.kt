package com.questlog.core.domain.usecase.memory

import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.data.remote.ClaudeApiService
import com.questlog.core.data.remote.dto.ClaudeMessage
import com.questlog.core.data.remote.dto.ClaudeMessageRequest
import com.questlog.core.data.remote.prompts.MemoryEnrichmentPrompts
import com.questlog.core.data.secure.SecureStorage
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.core.domain.repository.MemoryRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Claude AI 로 기억 본문을 윤색하고 enrichedBody 를 업데이트한다.
 *
 * 프라이버시 계약 (CLAUDE.md):
 * - canCallApi() 확인 없이 API 호출 금지
 * - PromptSanitizer: body 텍스트만 전송. taskTitleSnapshot / outcomeType / characterId 미포함
 * - API 실패 시 null 반환 (enrichedBody 업데이트 안 함) — 사용자 경험 보호
 *
 * 반환값: 윤색된 텍스트(성공) 또는 null(거부/실패)
 */
class EnrichMemoryWithClaudeUseCase @Inject constructor(
    private val consentManager: ConsentManager,
    private val api: ClaudeApiService,
    private val secureStorage: SecureStorage,
    private val memoryRepository: MemoryRepository,
) {
    companion object {
        private const val MODEL = "claude-haiku-4-5-20251001"
        private const val MAX_TOKENS = 512
    }

    suspend operator fun invoke(entry: MemoryEntry): String? {
        // 1) 동의 + 활성화 이중 확인 (CLAUDE.md 프라이버시 계약)
        if (!consentManager.canCallApi()) {
            Timber.d("EnrichMemory: API 호출 거부 (동의 없음 또는 비활성)")
            return null
        }

        val apiKey = secureStorage.getApiKey() ?: run {
            Timber.d("EnrichMemory: API key 없음")
            return null
        }

        // 2) body 만 전송 — sanitize 계약 (taskTitleSnapshot/outcomeType/characterId 미포함)
        val prompt = MemoryEnrichmentPrompts.build(
            body = entry.body,
            outcomeType = entry.outcomeType,
        )

        // 3) API 호출 — 실패 시 null 폴백
        val enriched = runCatching {
            val request = ClaudeMessageRequest(
                model = MODEL,
                maxTokens = MAX_TOKENS,
                system = prompt.system,
                messages = listOf(ClaudeMessage(role = "user", content = prompt.user)),
            )
            val response = api.generateMessage(apiKey = apiKey, request = request)
            response.content.firstOrNull { it.type == "text" }?.text?.takeIf { it.isNotBlank() }
        }.getOrElse { e ->
            Timber.e(e, "EnrichMemory: Claude API 호출 실패, enrichedBody 업데이트 생략")
            null
        }

        // 4) 성공 시에만 DB 업데이트
        if (enriched != null) {
            memoryRepository.updateEnrichedBody(entry.id, enriched)
        }

        return enriched
    }
}
