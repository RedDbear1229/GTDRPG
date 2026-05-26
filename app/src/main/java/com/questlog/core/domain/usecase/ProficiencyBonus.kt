package com.questlog.core.domain.usecase

// D&D 5e 숙련 보너스: Lv1-4 → +2, Lv5-8 → +3, Lv9-12 → +4, Lv13-16 → +5, Lv17-20 → +6.
// 공식: 2 + (level - 1) / 4 (level >= 1 가정).
object ProficiencyBonus {
    fun forLevel(level: Int): Int {
        require(level >= 1) { "level must be >= 1 (got $level)" }
        return 2 + (level - 1) / 4
    }
}
