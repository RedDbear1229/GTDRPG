package com.questlog.core.data.mapper

import com.questlog.core.data.db.entity.CombatLogEntity
import com.questlog.core.domain.model.CombatLog

fun CombatLog.toEntity() = CombatLogEntity(
    id = id,
    taskId = taskId,
    characterId = characterId,
    d20Result = d20Result,
    totalAttack = totalAttack,
    monsterAC = monsterAC,
    xpGained = xpGained,
    hpLost = hpLost,
    isCriticalHit = isCriticalHit,
    isCriticalMiss = isCriticalMiss,
    rolledAt = rolledAt,
)

fun CombatLogEntity.toDomain() = CombatLog(
    id = id,
    taskId = taskId,
    characterId = characterId,
    d20Result = d20Result,
    totalAttack = totalAttack,
    monsterAC = monsterAC,
    xpGained = xpGained,
    hpLost = hpLost,
    isCriticalHit = isCriticalHit,
    isCriticalMiss = isCriticalMiss,
    rolledAt = rolledAt,
)
