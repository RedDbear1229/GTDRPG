package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

// taskId UNIQUE — 태스크 1개당 전투 로그 1개. INSERT OR IGNORE 로 중복 차단 (CLAUDE.md §데이터 무결성).
// taskId FK = SET_NULL: 태스크 삭제 시 로그는 보존, taskId 만 null 처리.
// characterId FK = CASCADE: 캐릭터 삭제 시 전투 기록 전체 삭제.
@Entity(
    tableName = "combat_logs",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("taskId", unique = true),
        Index("characterId"),
        Index("rolledAt"),
    ],
)
data class CombatLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
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
