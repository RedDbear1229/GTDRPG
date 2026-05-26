package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.Task
import kotlin.random.Random

// docs/04_game_mechanics.md §4.1 SSOT.
// random 주입으로 단위 테스트 가능 (RollAbilityScoresUseCase 선례 동일).
class ResolveCombatUseCase(
    private val random: Random = Random.Default,
) {

    operator fun invoke(task: Task, character: Character): CombatResult {
        val d20 = random.nextInt(1, 21)

        if (d20 == 20) return buildCriticalHit(task, character)
        if (d20 == 1) return buildCriticalMiss(task.challengeRating, character)

        val abilityMod = abilityModifier(task.primaryAbility, character)
        val totalAttack = d20 + abilityMod + character.proficiencyBonus
        val monsterAC = monsterAC(task.challengeRating)

        return if (totalAttack >= monsterAC) {
            CombatResult.Hit(
                d20Result = d20,
                totalAttack = totalAttack,
                monsterAC = monsterAC,
                xpGained = calculateXp(task, character, isCritical = false),
            )
        } else {
            CombatResult.Miss(
                d20Result = d20,
                totalAttack = totalAttack,
                monsterAC = monsterAC,
                hpLost = hpLoss(task.challengeRating, isCritical = false),
            )
        }
    }

    private fun buildCriticalHit(task: Task, character: Character) = CombatResult.CriticalHit(
        xpGained = calculateXp(task, character, isCritical = true),
        narrative = CRIT_NARRATIVES.random(random),
    )

    private fun buildCriticalMiss(cr: Float, character: Character) = CombatResult.CriticalMiss(
        hpLost = hpLoss(cr, isCritical = true),
        humorousMessage = CRIT_MISS_MESSAGES.random(random),
    )

    // docs/04_game_mechanics.md §4.1 getMonsterAC
    internal fun monsterAC(cr: Float): Int = when {
        cr < 1f  -> 10; cr < 2f  -> 11; cr < 3f  -> 12; cr < 4f  -> 13
        cr < 6f  -> 14; cr < 8f  -> 15; cr < 10f -> 16; cr < 12f -> 17
        cr < 15f -> 18; cr < 18f -> 19; cr < 21f -> 20; else -> 22
    }

    // docs/04_game_mechanics.md §4.1 calculateXP
    internal fun calculateXp(task: Task, character: Character, isCritical: Boolean): Long {
        val base = (task.challengeRating * 25).toLong()
        val crit = if (isCritical) 2f else 1f
        val deadline = task.dueDate?.let { due ->
            if (due >= System.currentTimeMillis()) 1.2f else 0.9f
        } ?: 1f
        val streak = 1f + (character.streakDays * 0.05f).coerceAtMost(0.5f)
        return (base * crit * deadline * streak).toLong().coerceAtLeast(1)
    }

    // docs/04_game_mechanics.md §4.1 calculateHPLoss (장비 보너스는 Phase 4)
    internal fun hpLoss(cr: Float, isCritical: Boolean): Int {
        val base = (cr * 1.5f).toInt().coerceAtLeast(1)
        return if (isCritical) base * 2 else base
    }

    private fun abilityModifier(ability: AbilityType, character: Character): Int {
        val score = when (ability) {
            AbilityType.STR -> character.strength
            AbilityType.DEX -> character.dexterity
            AbilityType.CON -> character.constitution
            AbilityType.INT -> character.intelligence
            AbilityType.WIS -> character.wisdom
            AbilityType.CHA -> character.charisma
        }
        return AbilityCalculator.modifier(score)
    }

    companion object {
        private val CRIT_NARRATIVES = listOf(
            "완벽한 일격! 몬스터가 산산조각 났다!",
            "전설의 기술! 모든 이들이 경탄했다!",
            "천둥벼락 같은 공격! 신화가 탄생했다!",
            "그 어떤 방어도 소용없었다. 순수한 힘의 승리!",
            "D20이 20을 가리켰다. 운명이 편을 들었다!",
        )
        private val CRIT_MISS_MESSAGES = listOf(
            "발을 헛디뎌 넘어졌다... 몬스터가 비웃는 것 같다.",
            "공격이 빗나가 근처 나무를 베었다. 나무는 억울하다.",
            "무기가 손에서 미끄러졌다. 다음엔 장갑을 끼자.",
            "눈을 감고 공격했다. 이건 비밀로 하자.",
            "크리티컬 미스! 자존심에 가장 큰 타격을 입었다.",
        )
    }
}
