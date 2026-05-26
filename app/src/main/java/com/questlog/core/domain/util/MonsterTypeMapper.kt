package com.questlog.core.domain.util

import com.questlog.core.domain.model.MonsterType

// docs/04_game_mechanics.md §4.6 — CR ↔ MonsterType 매핑. 가장 가까운 (defaultCr ≤ cr) 몬스터 선택.
object MonsterTypeMapper {

    private val tiers: List<MonsterType> = MonsterType.values().sortedBy { it.defaultCr }

    fun fromCr(cr: Float): MonsterType =
        tiers.lastOrNull { it.defaultCr <= cr } ?: tiers.first()
}
