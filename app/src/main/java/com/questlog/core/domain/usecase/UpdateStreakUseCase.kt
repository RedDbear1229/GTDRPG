package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

// docs/04_game_mechanics.md §4.3 SSOT.
// 자정 Worker에서 호출: "어제" 퀘스트 완료 여부 판단 후 streak 갱신.
// ZoneId 는 항상 런타임에 조회 (사용자 시간대 변경 대응).
@Singleton
class UpdateStreakUseCase @Inject constructor() {

    data class Result(
        val character: Character,
        val milestone: StreakMilestone? = null,
    )

    operator fun invoke(character: Character, now: Long = System.currentTimeMillis()): Result {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val yesterday = today.minusDays(1)

        val lastActivity = character.lastActivityDate
            ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }

        val completedYesterday = lastActivity == yesterday

        val updatedCharacter = if (completedYesterday) {
            val newStreak = character.streakDays + 1
            character.copy(
                streakDays = newStreak,
                longestStreak = maxOf(character.longestStreak, newStreak),
                updatedAt = now,
            )
        } else {
            // streak 보호 토큰 소모
            if (character.streakProtectTokens > 0) {
                character.copy(
                    streakProtectTokens = character.streakProtectTokens - 1,
                    updatedAt = now,
                )
            } else {
                character.copy(streakDays = 0, updatedAt = now)
            }
        }

        val milestone = if (completedYesterday) {
            StreakMilestone.of(updatedCharacter.streakDays)
        } else null

        return Result(updatedCharacter, milestone)
    }
}

enum class StreakMilestone(val days: Int, val bonusXp: Long) {
    WEEK(7, 100L),
    MONTH(30, 500L),
    CENTURY(100, 2000L);

    companion object {
        fun of(streakDays: Int): StreakMilestone? =
            entries.firstOrNull { it.days == streakDays }
    }
}
