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
        challengeRating = 10f,  // AC=16, 높아서 기본 Miss 가능
        monsterType = MonsterType.ORC,
    )

    // D20 = 5, STR=0, prof=2 → 7 < AC16 = Miss (버프 없으면)
    private fun missRoll() = object : Random() {
        override fun nextBits(bitCount: Int): Int = 4  // nextInt(1,21) = 5
    }

    @Test
    fun `GUARANTEED_HIT 버프 — D20 낮아도 Hit 반환`() {
        val useCase = ResolveCombatUseCase(random = missRoll())
        val result = useCase(task, character, activeBuff = "GUARANTEED_HIT:1")
        assertTrue(result is CombatResult.Hit, "GUARANTEED_HIT 버프 시 항상 Hit이어야 함")
    }

    @Test
    fun `ATTACK_BONUS 버프 — 충분한 보너스로 Miss → Hit 전환`() {
        // D20=5, STR=0, prof=2 → 7 < AC16. +9 보너스면 16 = Hit
        val useCase = ResolveCombatUseCase(random = missRoll())
        val noBuffResult = useCase(task, character)
        val buffResult = useCase(task, character, activeBuff = "ATTACK_BONUS:9")

        assertTrue(noBuffResult is CombatResult.Miss)
        assertTrue(buffResult is CombatResult.Hit, "ATK +9 버프 시 Hit이어야 함")
    }

    @Test
    fun `XP_MULTIPLIER 버프 — XP 1_5배 적용`() {
        // D20 = 20 강제 (크리티컬)
        val critRoll = object : Random() {
            override fun nextBits(bitCount: Int): Int = 19  // nextInt(1,21) = 20
        }
        val useCase1 = ResolveCombatUseCase(random = critRoll)
        val useCase2 = ResolveCombatUseCase(random = critRoll)

        val base = (useCase1(task, character) as CombatResult.CriticalHit).xpGained
        val boosted = (useCase2(task, character, activeBuff = "XP_MULTIPLIER:150") as CombatResult.CriticalHit).xpGained
        assertEquals((base * 1.5f).toLong(), boosted)
    }

    @Test
    fun `DAMAGE_REDUCE 버프 — Miss HP 피해 50% 감소`() {
        val useCase1 = ResolveCombatUseCase(random = missRoll())
        val useCase2 = ResolveCombatUseCase(random = missRoll())

        val rawMiss = (useCase1(task, character) as CombatResult.Miss).hpLost
        val reduced = (useCase2(task, character, activeBuff = "DAMAGE_REDUCE:50") as CombatResult.Miss).hpLost
        assertEquals((rawMiss * 0.5f).toInt(), reduced)
    }

    @Test
    fun `CRIT_THRESHOLD 버프 — D20 = 15 크리티컬 처리`() {
        // D20 = 15 강제
        val roll15 = object : Random() {
            override fun nextBits(bitCount: Int): Int = 14  // nextInt(1,21) = 15
        }
        val useCase = ResolveCombatUseCase(random = roll15)
        val result = useCase(task, character, activeBuff = "CRIT_THRESHOLD:15")
        assertTrue(result is CombatResult.CriticalHit, "D20=15이면 크리티컬이어야 함")
    }

    @Test
    fun `버프 없으면 기존 전투 결과와 동일`() {
        val useCase1 = ResolveCombatUseCase(random = missRoll())
        val useCase2 = ResolveCombatUseCase(random = missRoll())
        val withNull = useCase1(task, character, activeBuff = null)
        val withoutParam = useCase2(task, character)
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
