package com.questlog.core.data.repository

import com.questlog.core.data.budget.ApiBudget
import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.data.remote.ClaudeApiService
import com.questlog.core.data.remote.dto.ClaudeMessage
import com.questlog.core.data.remote.dto.ClaudeMessageRequest
import com.questlog.core.data.remote.prompts.ClaudePrompts
import com.questlog.core.data.remote.prompts.LocalNarratives
import com.questlog.core.data.remote.prompts.PromptPair
import com.questlog.core.data.sanitizer.PromptSanitizer
import com.questlog.core.data.sanitizer.SanitizedTask
import com.questlog.core.data.secure.SecureStorage
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.repository.ClaudeRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeRepositoryImpl @Inject constructor(
    private val api: ClaudeApiService,
    private val consentManager: ConsentManager,
    private val apiBudget: ApiBudget,
    private val secureStorage: SecureStorage,
    private val sanitizer: PromptSanitizer,
) : ClaudeRepository {

    companion object {
        private const val MODEL_STANDARD = "claude-sonnet-4-6"
        private const val MODEL_FAST = "claude-haiku-4-5-20251001"
        private const val MAX_TOKENS_STANDARD = 256
        private const val MAX_TOKENS_FAST = 128
    }

    override suspend fun generateCombatNarrative(
        character: Character,
        task: SanitizedTask,
        result: CombatResult.Hit,
    ): String = callApi(
        prompt = ClaudePrompts.combatNarrative(character, task, result),
        model = MODEL_STANDARD,
        maxTokens = MAX_TOKENS_STANDARD,
    ) {
        LocalNarratives.COMBAT_HIT.random().let { t ->
            LocalNarratives.fill(t, "name" to character.name, "taskTitle" to task.title, "xp" to result.xpGained.toString())
        }
    }

    override suspend fun generateCriticalHitNarrative(
        character: Character,
        task: SanitizedTask,
        xpGained: Long,
        itemName: String?,
    ): String = callApi(
        prompt = ClaudePrompts.criticalHit(character, task, xpGained, itemName),
        model = MODEL_STANDARD,
        maxTokens = MAX_TOKENS_STANDARD,
    ) {
        LocalNarratives.CRITICAL_HIT.random().let { t ->
            LocalNarratives.fill(t, "name" to character.name, "taskTitle" to task.title, "xp" to xpGained.toString())
        }
    }

    override suspend fun generateCriticalMissNarrative(
        character: Character,
        task: SanitizedTask,
        hpLost: Int,
    ): String = callApi(
        prompt = ClaudePrompts.criticalMiss(character, task, hpLost),
        model = MODEL_FAST,
        maxTokens = MAX_TOKENS_FAST,
    ) {
        LocalNarratives.CRITICAL_MISS.random()
    }

    override suspend fun generateWeeklyReviewSummary(
        character: Character,
        completedCount: Int,
        xpGained: Long,
        critCount: Int,
        missCount: Int,
        unfinished: Int,
        weekLabel: String,
    ): String = callApi(
        prompt = ClaudePrompts.weeklyReview(character, completedCount, xpGained, critCount, missCount, unfinished, weekLabel),
        model = MODEL_STANDARD,
        maxTokens = MAX_TOKENS_STANDARD,
    ) {
        LocalNarratives.WEEKLY_REVIEW.random().let { t ->
            LocalNarratives.fill(t, "name" to character.name, "completedCount" to completedCount.toString(), "xp" to xpGained.toString())
        }
    }

    override suspend fun getClarifySuggestions(rawText: String, character: Character): List<String> {
        val sanitizedText = sanitizer.sanitizeTaskTitle(rawText)
        val narrative = callApi(
            prompt = ClaudePrompts.clarify(sanitizedText, character),
            model = MODEL_FAST,
            maxTokens = MAX_TOKENS_FAST,
        ) { "" }
        if (narrative.isBlank()) return emptyList()
        return narrative.lines()
            .map { it.trim().trimStart('-', '•', '·', ' ') }
            .filter { it.isNotBlank() }
            .take(3)
    }

    override suspend fun generateLevelUpMessage(
        character: Character,
        newLevel: Int,
        totalQuests: Int,
    ): String = callApi(
        prompt = ClaudePrompts.levelUp(character, newLevel, totalQuests),
        model = MODEL_STANDARD,
        maxTokens = MAX_TOKENS_STANDARD,
    ) {
        LocalNarratives.LEVEL_UP.random().let { t ->
            LocalNarratives.fill(t, "name" to character.name, "level" to newLevel.toString(), "className" to character.classType.label, "totalQuests" to totalQuests.toString())
        }
    }

    override suspend fun generateEncounterNarrative(character: Character, todayCompleted: Int): String =
        callApi(
            prompt = ClaudePrompts.encounter(character, todayCompleted),
            model = MODEL_FAST,
            maxTokens = MAX_TOKENS_FAST,
        ) {
            LocalNarratives.ENCOUNTER.random()
        }

    private suspend fun callApi(
        prompt: PromptPair,
        model: String,
        maxTokens: Int,
        fallback: () -> String,
    ): String {
        if (!consentManager.canCallApi()) return fallback()
        if (!apiBudget.tryReserve()) return fallback()

        val apiKey = secureStorage.getApiKey() ?: return fallback()

        return runCatching {
            val request = ClaudeMessageRequest(
                model = model,
                maxTokens = maxTokens,
                system = prompt.system,
                messages = listOf(ClaudeMessage(role = "user", content = prompt.user)),
            )
            val response = api.generateMessage(apiKey = apiKey, request = request)
            val text = response.content.firstOrNull { it.type == "text" }?.text.orEmpty()
            text.ifBlank { fallback() }
        }.getOrElse { e ->
            Timber.e(e, "Claude API call failed")
            fallback()
        }
    }
}
