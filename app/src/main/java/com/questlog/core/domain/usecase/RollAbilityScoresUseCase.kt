package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import javax.inject.Inject
import kotlin.random.Random

data class AbilityScores(
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
) {
    operator fun get(ability: AbilityType): Int = when (ability) {
        AbilityType.STR -> strength
        AbilityType.DEX -> dexterity
        AbilityType.CON -> constitution
        AbilityType.INT -> intelligence
        AbilityType.WIS -> wisdom
        AbilityType.CHA -> charisma
    }
}

// 4d6 drop lowest: 주사위 4개 굴린 뒤 최솟값 하나 제거, 나머지 3개 합산.
// docs/04_game_mechanics.md §4.2 SSOT. random 주입으로 단위 테스트 가능.
class RollAbilityScoresUseCase(
    private val random: Random,
) {
    @Inject constructor() : this(Random.Default)
    fun rollAll(): AbilityScores = AbilityScores(
        strength = rollOne(),
        dexterity = rollOne(),
        constitution = rollOne(),
        intelligence = rollOne(),
        wisdom = rollOne(),
        charisma = rollOne(),
    )

    fun rollOne(): Int {
        val dice = List(4) { random.nextInt(1, 7) }
        return dice.sum() - dice.min()
    }
}
