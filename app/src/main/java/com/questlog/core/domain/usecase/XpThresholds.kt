package com.questlog.core.domain.usecase

// D&D 5e 누적 XP 임계값 (Lv1=0 부터 Lv20=355,000). docs/04_game_mechanics.md §4.3 SSOT.
// Lv20 cap — 초과 XP 는 totalXpEarned 에만 누적, level 은 20 에서 멈춘다 (PRD §3.2).
object XpThresholds {

    private val CUMULATIVE = longArrayOf(
        0L,        // Lv1 시작
        300L,      // Lv2
        900L,      // Lv3
        2_700L,    // Lv4
        6_500L,    // Lv5
        14_000L,   // Lv6
        23_000L,   // Lv7
        34_000L,   // Lv8
        48_000L,   // Lv9
        64_000L,   // Lv10
        85_000L,   // Lv11
        100_000L,  // Lv12
        120_000L,  // Lv13
        140_000L,  // Lv14
        165_000L,  // Lv15
        195_000L,  // Lv16
        225_000L,  // Lv17
        265_000L,  // Lv18
        305_000L,  // Lv19
        355_000L,  // Lv20
    )

    const val MAX_LEVEL: Int = 20

    fun cumulativeForLevel(level: Int): Long {
        require(level in 1..MAX_LEVEL) { "level must be 1..$MAX_LEVEL (got $level)" }
        return CUMULATIVE[level - 1]
    }

    fun levelForXp(totalXp: Long): Int {
        require(totalXp >= 0) { "totalXp must be >= 0 (got $totalXp)" }
        for (lvl in MAX_LEVEL downTo 1) {
            if (totalXp >= CUMULATIVE[lvl - 1]) return lvl
        }
        return 1
    }

    fun xpToNextLevel(totalXp: Long): Long? {
        val current = levelForXp(totalXp)
        if (current >= MAX_LEVEL) return null
        return CUMULATIVE[current] - totalXp
    }
}
