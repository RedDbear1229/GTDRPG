package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

// 주당 1개 보장: weekStart UNIQUE 제약. INSERT OR IGNORE 로 중복 차단.
@Entity(
    tableName = "weekly_reviews",
    indices = [Index(value = ["weekStart"], unique = true)],
)
data class WeeklyReviewEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val weekStart: String,       // ISO date "2026-05-25" (월요일 기준)
    val weekLabel: String,       // "5월 4주차"
    val completedCount: Int,
    val xpGained: Long,
    val critCount: Int,
    val missCount: Int,
    val unfinishedCount: Int,
    val aiSummary: String? = null,
    val xpReward: Long = REWARD_XP,
    val completedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val REWARD_XP = 200L
    }
}
