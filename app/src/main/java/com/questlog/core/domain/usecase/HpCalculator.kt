package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass

// docs/04_game_mechanics.md §4.2 SSOT.
// baseHP = hitDie + conMod (Lv1 최대값)
// 이후 레벨당 +(hitDie/2 + 1 + conMod) (평균값)
// CON 수정치가 매우 낮을 때 HP 가 0 이하로 떨어지지 않도록 minOf 1 보장.
object HpCalculator {

    fun maxHp(classType: CharacterClass, level: Int, constitutionScore: Int): Int {
        require(level >= 1) { "level must be >= 1 (got $level)" }
        val hitDie = classType.hitDie
        val conMod = AbilityCalculator.modifier(constitutionScore)
        val baseHp = hitDie + conMod
        val perLevel = hitDie / 2 + 1 + conMod
        val total = baseHp + (level - 1) * perLevel
        return total.coerceAtLeast(1)
    }
}
