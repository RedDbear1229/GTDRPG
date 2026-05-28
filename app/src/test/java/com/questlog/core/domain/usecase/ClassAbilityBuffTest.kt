package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.ClassAbilityDef
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ClassAbilityBuffTest {

    private val character = Character(
        name = "Hero", classType = CharacterClass.FIGHTER, level = 1,
        maxHp = 10, currentHp = 10,
        strength = 10, dexterity = 10, constitution = 10,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private val task = Task(
        title = "테스트",
        status = TaskStatus.ACTIVE,
        lifeArea = LifeArea.WORK,
        primaryAbility = AbilityType.STR,
        challengeRating = 10f,  // monsterAC = 17 (cr < 12f branch)
        monsterType = MonsterType.ORC,
    )

    // Builds a useCase with d20 pinned to `fixedD20`. Item drops use a separate seeded random.
    private fun makeUseCase(fixedD20: Int) = ResolveCombatUseCase(
        random = object : Random() {
            override fun nextBits(bitCount: Int): Int =
                if (bitCount == 0) 0 else (fixedD20 - 1) and ((1 shl bitCount) - 1)
        },
        itemDropUseCase = ItemDropUseCase(Random(0)),
    )

    @Test
    fun `GUARANTEED_HIT 버프 — D20 낮아도 Hit 반환`() {
        val result = makeUseCase(5)(task, character, activeBuff = "GUARANTEED_HIT:1")
        assertTrue(result is CombatResult.Hit, "GUARANTEED_HIT 버프 시 항상 Hit이어야 함")
    }

    @Test
    fun `ATTACK_BONUS 버프 — 충분한 보너스로 Miss → Hit 전환`() {
        // D20=5, STR=0, prof=2 → totalAttack=7 < AC17. +10 보너스면 17 = Hit
        val noBuffResult = makeUseCase(5)(task, character)
        val buffResult = makeUseCase(5)(task, character, activeBuff = "ATTACK_BONUS:10")

        assertTrue(noBuffResult is CombatResult.Miss)
        assertTrue(buffResult is CombatResult.Hit, "ATK +10 버프 시 Hit이어야 함")
    }

    @Test
    fun `XP_MULTIPLIER 버프 — XP 1_5배 적용`() {
        val base = (makeUseCase(20)(task, character) as CombatResult.CriticalHit).xpGained
        val boosted = (makeUseCase(20)(task, character, activeBuff = "XP_MULTIPLIER:150") as CombatResult.CriticalHit).xpGained
        assertEquals((base * 1.5f).toLong(), boosted)
    }

    @Test
    fun `DAMAGE_REDUCE 버프 — Miss HP 피해 50% 감소`() {
        val rawMiss = (makeUseCase(5)(task, character) as CombatResult.Miss).hpLost
        val reduced = (makeUseCase(5)(task, character, activeBuff = "DAMAGE_REDUCE:50") as CombatResult.Miss).hpLost
        assertEquals((rawMiss * 0.5f).toInt(), reduced)
    }

    @Test
    fun `CRIT_THRESHOLD 버프 — D20 = 15 크리티컬 처리`() {
        val result = makeUseCase(15)(task, character, activeBuff = "CRIT_THRESHOLD:15")
        assertTrue(result is CombatResult.CriticalHit, "D20=15이면 크리티컬이어야 함")
    }

    @Test
    fun `버프 없으면 기존 전투 결과와 동일`() {
        val withNull = makeUseCase(5)(task, character, activeBuff = null)
        val withoutParam = makeUseCase(5)(task, character)
        assertEquals(withNull::class, withoutParam::class)
    }

    @Test
    fun `각 클래스 능력 buffCode 파싱 가능 확인`() {
        CharacterClass.entries.forEach { cls ->
            val ability = ClassAbilityDef.forClass(cls)
            val code = ability.buffCode
            assertTrue(code.contains(":"), "$cls buffCode 포맷 불량: $code")
        }
    }
}
