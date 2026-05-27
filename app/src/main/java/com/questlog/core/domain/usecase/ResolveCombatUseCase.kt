package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.BuffEffectType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.model.Task
import kotlin.random.Random

// docs/04_game_mechanics.md §4.1 SSOT.
// equippedItems: F4.1 에서 추가 — 장비 보너스 적용. 기본값 emptyList() 로 기존 테스트 비파괴.
// itemDrop 계산은 ItemDropUseCase 위임.
class ResolveCombatUseCase(
    private val random: Random = Random.Default,
    private val itemDropUseCase: ItemDropUseCase = ItemDropUseCase(random),
) {

    operator fun invoke(
        task: Task,
        character: Character,
        equippedItems: List<Item> = emptyList(),
        activeBuff: String? = null,
    ): CombatResult {
        val buff = parseBuff(activeBuff)
        val d20 = random.nextInt(1, 21)

        // GUARANTEED_HIT 버프: 무조건 Hit 처리 (크리티컬 아님)
        if (buff?.first == BuffEffectType.GUARANTEED_HIT) {
            val monsterAC = monsterAC(task.challengeRating)
            return CombatResult.Hit(
                d20Result = d20,
                totalAttack = monsterAC,
                monsterAC = monsterAC,
                xpGained = applyXpBuff(calculateXp(task, character, equippedItems, isCritical = false), buff),
                itemDrop = itemDropUseCase(task.challengeRating, isCriticalHit = false),
            )
        }

        // CRIT_THRESHOLD 버프: D20 ≥ threshold 시 크리티컬
        val critThreshold = if (buff?.first == BuffEffectType.CRIT_THRESHOLD) buff.second else 20
        if (d20 >= critThreshold) return buildCriticalHit(task, character, equippedItems, buff)
        if (d20 == 1) return buildCriticalMiss(task.challengeRating, character)

        val weaponBonus = equippedWeaponBonus(equippedItems)
        val attackBuff = if (buff?.first == BuffEffectType.ATTACK_BONUS) buff.second else 0
        val abilityMod = abilityModifier(task.primaryAbility, character)
        val totalAttack = d20 + abilityMod + character.proficiencyBonus + weaponBonus + attackBuff
        val monsterAC = monsterAC(task.challengeRating)

        return if (totalAttack >= monsterAC) {
            CombatResult.Hit(
                d20Result = d20,
                totalAttack = totalAttack,
                monsterAC = monsterAC,
                xpGained = applyXpBuff(calculateXp(task, character, equippedItems, isCritical = false), buff),
                itemDrop = itemDropUseCase(task.challengeRating, isCriticalHit = false),
            )
        } else {
            val rawHpLost = hpLoss(task.challengeRating, isCritical = false)
            val reducedHpLost = if (buff?.first == BuffEffectType.DAMAGE_REDUCE) {
                (rawHpLost * buff.second / 100f).toInt().coerceAtLeast(0)
            } else rawHpLost
            CombatResult.Miss(
                d20Result = d20,
                totalAttack = totalAttack,
                monsterAC = monsterAC,
                hpLost = reducedHpLost,
            )
        }
    }

    private fun buildCriticalHit(
        task: Task,
        character: Character,
        equippedItems: List<Item>,
        buff: Pair<BuffEffectType, Int>? = null,
    ) = CombatResult.CriticalHit(
        xpGained = applyXpBuff(calculateXp(task, character, equippedItems, isCritical = true), buff),
        narrative = CRIT_NARRATIVES.random(random),
        itemDrop = itemDropUseCase(task.challengeRating, isCriticalHit = true),
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

    // docs/04_game_mechanics.md §4.1 calculateXP (장비 XP 배율 F4.1 적용)
    internal fun calculateXp(
        task: Task,
        character: Character,
        equippedItems: List<Item>,
        isCritical: Boolean,
    ): Long {
        val base = (task.challengeRating * 25).toLong()
        val crit = if (isCritical) 2f else 1f
        val deadline = task.dueDate?.let { due ->
            if (due >= System.currentTimeMillis()) 1.2f else 0.9f
        } ?: 1f
        val streak = 1f + (character.streakDays * 0.05f).coerceAtMost(0.5f)
        val equipment = equippedItems.fold(1.0f) { acc, item -> acc * item.xpMultiplier }
        return (base * crit * deadline * streak * equipment).toLong().coerceAtLeast(1)
    }

    // 이전 테스트 호환용 — equippedItems 없이 호출 시 사용
    internal fun calculateXp(task: Task, character: Character, isCritical: Boolean): Long =
        calculateXp(task, character, emptyList(), isCritical)

    private fun applyXpBuff(base: Long, buff: Pair<BuffEffectType, Int>?): Long {
        if (buff?.first != BuffEffectType.XP_MULTIPLIER) return base
        return (base * buff.second / 100f).toLong().coerceAtLeast(1)
    }

    private fun parseBuff(code: String?): Pair<BuffEffectType, Int>? {
        if (code.isNullOrBlank()) return null
        return runCatching {
            val parts = code.split(":")
            BuffEffectType.valueOf(parts[0]) to (parts.getOrNull(1)?.toInt() ?: 0)
        }.getOrNull()
    }

    internal fun hpLoss(cr: Float, isCritical: Boolean): Int {
        val base = (cr * 1.5f).toInt().coerceAtLeast(1)
        return if (isCritical) base * 2 else base
    }

    private fun equippedWeaponBonus(items: List<Item>): Int =
        items.firstOrNull { it.equippedSlot == EquipmentSlot.WEAPON }?.attackBonus ?: 0

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
