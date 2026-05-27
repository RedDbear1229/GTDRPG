package com.questlog.core.domain.model

// docs/04_game_mechanics.md §4.1 SSOT.
// itemDrop: F4.1(Phase 4) 에서 추가 — Hit/CriticalHit 모두 드롭 가능.
sealed class CombatResult {
    data class Hit(
        val d20Result: Int,
        val totalAttack: Int,
        val monsterAC: Int,
        val xpGained: Long,
        val itemDrop: ItemTemplate? = null,
    ) : CombatResult()

    data class CriticalHit(
        val d20Result: Int = 20,
        val xpGained: Long,
        val narrative: String,
        val itemDrop: ItemTemplate? = null,
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
