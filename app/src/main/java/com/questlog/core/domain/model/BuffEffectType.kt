package com.questlog.core.domain.model

enum class BuffEffectType {
    ATTACK_BONUS,    // 다음 전투 totalAttack += value
    XP_MULTIPLIER,   // 다음 전투 XP × (value / 100f)
    DAMAGE_REDUCE,   // 다음 전투 Miss HP 피해 × (value / 100f)
    GUARANTEED_HIT,  // 다음 전투 무조건 Hit (value 무시)
    CRIT_THRESHOLD,  // 다음 전투 D20 ≥ value 시 크리티컬
    HP_RESTORE,      // 즉시 HP maxHp × (value / 100)% 회복
}
