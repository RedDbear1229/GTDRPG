package com.questlog.core.domain.model

// docs/04_game_mechanics.md §4.2 HP 상태 효과 SSOT.
enum class HpStatus {
    HEALTHY,      // > 75%
    TIRED,        // 50-75%
    WOUNDED,      // 25-50%
    CRITICAL,     // 0-25%
    UNCONSCIOUS;  // 0%

    companion object {
        fun of(currentHp: Int, maxHp: Int): HpStatus {
            if (maxHp <= 0) return UNCONSCIOUS
            val pct = currentHp.toFloat() / maxHp
            return when {
                currentHp <= 0  -> UNCONSCIOUS
                pct > 0.75f     -> HEALTHY
                pct > 0.50f     -> TIRED
                pct > 0.25f     -> WOUNDED
                else            -> CRITICAL
            }
        }
    }
}
