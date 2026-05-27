package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.HpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class UpdateStreakUseCaseTest {

    private val useCase = UpdateStreakUseCase()
    private val zone = ZoneId.systemDefault()

    private fun character(
        streakDays: Int = 0,
        longestStreak: Int = 0,
        lastActivityDate: Long? = null,
        streakProtectTokens: Int = 0,
    ) = testCharacter(
        streakDays = streakDays,
        longestStreak = longestStreak,
        lastActivityDate = lastActivityDate,
        streakProtectTokens = streakProtectTokens,
    )

    /** now = 자정 직후 (새날 0:00:01) */
    private fun nowMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli() + 1_000L

    @Test
    fun `어제 퀘스트 완료 → streak 증가`() {
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val lastActivity = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
        val c = character(streakDays = 3, lastActivityDate = lastActivity)

        val result = useCase(c, nowMillis(today))

        assertEquals(4, result.character.streakDays)
    }

    @Test
    fun `7일 달성 시 WEEK 마일스톤 반환`() {
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val lastActivity = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
        val c = character(streakDays = 6, lastActivityDate = lastActivity)

        val result = useCase(c, nowMillis(today))

        assertEquals(StreakMilestone.WEEK, result.milestone)
    }

    @Test
    fun `미완료 토큰 없으면 streak 리셋`() {
        val today = LocalDate.now(zone)
        val twoDaysAgo = today.minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli()
        val c = character(streakDays = 10, lastActivityDate = twoDaysAgo, streakProtectTokens = 0)

        val result = useCase(c, nowMillis(today))

        assertEquals(0, result.character.streakDays)
        assertNull(result.milestone)
    }

    @Test
    fun `미완료 토큰 있으면 streak 유지하고 토큰 소모`() {
        val today = LocalDate.now(zone)
        val twoDaysAgo = today.minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli()
        val c = character(streakDays = 10, lastActivityDate = twoDaysAgo, streakProtectTokens = 2)

        val result = useCase(c, nowMillis(today))

        assertEquals(10, result.character.streakDays)
        assertEquals(1, result.character.streakProtectTokens)
    }

    @Test
    fun `longestStreak 는 streak 보다 작아질 수 없다`() {
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val c = character(streakDays = 29, longestStreak = 29, lastActivityDate = yesterday)

        val result = useCase(c, nowMillis(today))

        assertEquals(30, result.character.streakDays)
        assertEquals(30, result.character.longestStreak)
    }
}

// 테스트용 최소 Character 생성 헬퍼
private fun testCharacter(
    streakDays: Int = 0,
    longestStreak: Int = 0,
    lastActivityDate: Long? = null,
    streakProtectTokens: Int = 0,
) = com.questlog.core.domain.model.Character(
    name = "Tester",
    classType = CharacterClass.FIGHTER,
    level = 1,
    currentXp = 0,
    totalXpEarned = 0,
    maxHp = 10,
    currentHp = 10,
    strength = 10,
    dexterity = 10,
    constitution = 10,
    intelligence = 10,
    wisdom = 10,
    charisma = 10,
    proficiencyBonus = 2,
    streakDays = streakDays,
    longestStreak = longestStreak,
    lastActivityDate = lastActivityDate,
    streakProtectTokens = streakProtectTokens,
)
