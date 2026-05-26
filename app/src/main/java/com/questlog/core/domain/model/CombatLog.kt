package com.questlog.core.domain.model

import java.util.UUID

// 완료된 전투의 불변 기록. INSERT 후 수정 금지 (CLAUDE.md §데이터 무결성).
data class CombatLog(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String?,
    val characterId: String,
    val d20Result: Int,
    val totalAttack: Int,
    val monsterAC: Int,
    val xpGained: Long,
    val hpLost: Int,
    val isCriticalHit: Boolean,
    val isCriticalMiss: Boolean,
    val rolledAt: Long = System.currentTimeMillis(),
)
