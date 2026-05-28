package com.questlog.core.domain.repository

import com.questlog.core.data.sanitizer.SanitizedTask
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatResult

interface ClaudeRepository {
    suspend fun generateCombatNarrative(
        character: Character,
        task: SanitizedTask,
        result: CombatResult.Hit,
    ): String

    suspend fun generateCriticalHitNarrative(
        character: Character,
        task: SanitizedTask,
        xpGained: Long,
        itemName: String?,
    ): String

    suspend fun generateCriticalMissNarrative(
        character: Character,
        task: SanitizedTask,
        hpLost: Int,
    ): String

    suspend fun generateWeeklyReviewSummary(
        character: Character,
        completedCount: Int,
        xpGained: Long,
        critCount: Int,
        missCount: Int,
        unfinished: Int,
        weekLabel: String,
    ): String

    suspend fun getClarifySuggestions(rawText: String, character: Character): List<String>

    suspend fun generateLevelUpMessage(
        character: Character,
        newLevel: Int,
        totalQuests: Int,
    ): String

    suspend fun generateEncounterNarrative(character: Character, todayCompleted: Int): String
}
