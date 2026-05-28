package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

// encounterId UNIQUE: 인카운터당 보상은 1회만. INSERT IGNORE + 반환값 검사로 멱등성 보장.
// 삭제하지 않는다 — 감사 불변 레코드 (CLAUDE.md §데이터 무결성).
@Entity(
    tableName = "xp_awards",
    indices = [
        Index("encounterId", unique = true),
        Index("characterId"),
    ],
)
data class XpAwardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val encounterId: String,
    val characterId: String,
    val xpAmount: Long,
    val awardedAt: Long = System.currentTimeMillis(),
)
