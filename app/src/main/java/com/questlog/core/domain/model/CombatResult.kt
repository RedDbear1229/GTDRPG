package com.questlog.core.domain.model

// docs/04_game_mechanics.md §4.1 SSOT.
// itemDrop 은 Phase 4 (F4.4) 에서 추가 — 현재는 항상 null placeholder.
sealed class CombatResult {
    data class Hit(
        val d20Result: Int,
        val totalAttack: Int,
        val monsterAC: Int,
        val xpGained: Long,
    ) : CombatResult()

    data class CriticalHit(
        val d20Result: Int = 20,
        val xpGained: Long,
        val narrative: String,
    ) : CombatResult()

    data class Miss(
        val d20Result: Int,
        val totalAttack: Int,
        val monsterAC: Int,
        val hpLost: Int,
    ) : CombatResult()

    data class CriticalMiss(
        val d20Result: Int = 1,
        val hpLost: Int,
        val humorousMessage: String,
    ) : CombatResult()
}
