package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ResolveCombatUseCaseTest {

    private val baseCharacter = Character(
        name = "Tester",
        classType = CharacterClass.FIGHTER,
        level = 1,
        maxHp = 12,
        currentHp = 12,
        strength = 16,  // +3 modifier
        dexterity = 10,
        constitution = 14,
        intelligence = 10,
        wisdom = 10,
        charisma = 10,
        proficiencyBonus = 2,
    )

    private val baseTask = Task(
        title = "테스트 퀘스트",
        status = TaskStatus.ACTIVE,
        lifeArea = LifeArea.WORK,
        primaryAbility = AbilityType.STR,
        challengeRating = 2f,
        monsterType = MonsterType.ORC,
    )

    private fun makeUseCase(fixedRoll: Int): ResolveCombatUseCase {
        val d20Random = object : Random() {
            override fun nextBits(bitCount: Int): Int = fixedRoll - 1
            // Override directly to avoid JVM `1 shl 32 == 1` masking bug in nextBits
            override fun nextInt(from: Int, until: Int): Int = fixedRoll.coerceIn(from, until - 1)
        }
        // Separate item-drop Random so list.random() never shares state with the d20 mock.
        return ResolveCombatUseCase(d20Random, ItemDropUseCase(Random(0)))
    }

    @Test
    fun `D20=20 이면 CriticalHit 반환`() {
        val result = makeUseCase(20)(baseTask, baseCharacter)
        assertInstanceOf(CombatResult.CriticalHit::class.java, result)
        assertEquals(20, (result as CombatResult.CriticalHit).d20Result)
    }

    @Test
    fun `D20=1 이면 CriticalMiss 반환`() {
        val result = makeUseCase(1)(baseTask, baseCharacter)
        assertInstanceOf(CombatResult.CriticalMiss::class.java, result)
        assertEquals(1, (result as CombatResult.CriticalMiss).d20Result)
    }

    @Test
    fun `공격 굴림이 AC 이상이면 Hit`() {
        // CR 2 → monsterAC = 12 (cr < 3f branch), STR+3 + profBonus+2 + D20 = 공격 굴림
        // D20=10 → totalAttack = 10+3+2=15 ≥ 12 → Hit
        val result = makeUseCase(10)(baseTask, baseCharacter)
        assertInstanceOf(CombatResult.Hit::class.java, result)
        val hit = result as CombatResult.Hit
        assertEquals(10, hit.d20Result)
        assertEquals(15, hit.totalAttack)
        assertEquals(12, hit.monsterAC)
        assertTrue(hit.xpGained > 0)
    }

    @Test
    fun `공격 굴림이 AC 미만이면 Miss`() {
        // CR 2 → monsterAC = 12, D20=2 → totalAttack = 2+3+2=7 < 12 → Miss
        val result = makeUseCase(2)(baseTask, baseCharacter)
        assertInstanceOf(CombatResult.Miss::class.java, result)
        val miss = result as CombatResult.Miss
        assertTrue(miss.hpLost > 0)
    }

    @Test
    fun `CriticalHit XP 는 Hit XP 의 2배`() {
        val useCase = ResolveCombatUseCase()
        val hitXp = useCase.calculateXp(baseTask, baseCharacter, isCritical = false)
        val critXp = useCase.calculateXp(baseTask, baseCharacter, isCritical = true)
        assertEquals(critXp, hitXp * 2)
    }

    @Test
    fun `CriticalMiss HP 손실은 일반 Miss 의 2배`() {
        val useCase = ResolveCombatUseCase()
        val missHp = useCase.hpLoss(2f, isCritical = false)
        val critHp = useCase.hpLoss(2f, isCritical = true)
        assertEquals(critHp, missHp * 2)
    }

    @Test
    fun `monsterAC CR 0_25 = 10`() {
        assertEquals(10, ResolveCombatUseCase().monsterAC(0.25f))
    }

    @Test
    fun `monsterAC CR 5 = 14`() {
        assertEquals(14, ResolveCombatUseCase().monsterAC(5f))
    }

    @Test
    fun `streakBonus 는 최대 1_5배 상한`() {
        val highStreak = baseCharacter.copy(streakDays = 100)
        val normalXp = ResolveCombatUseCase().calculateXp(baseTask, baseCharacter, isCritical = false)
        val streakXp = ResolveCombatUseCase().calculateXp(baseTask, highStreak, isCritical = false)
        // 최대 +50% → 1.5배 이하
        assertTrue(streakXp <= normalXp * 3)  // 크리티컬(×2) + 스트릭(×1.5) = 최대 3배
    }
}
