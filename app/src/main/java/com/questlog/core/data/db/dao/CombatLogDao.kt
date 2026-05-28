package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query

// 통계 쿼리용 DTO — Room @MapColumn / data class 매핑
data class D20Count(val d20Result: Int, val count: Int)
data class DayXp(val day: String, val xp: Long)

@Dao
interface CombatLogDao {
    @Query("SELECT COALESCE(SUM(xpGained), 0) FROM combat_logs WHERE rolledAt BETWEEN :start AND :end")
    suspend fun sumXpBetween(start: Long, end: Long): Long

    @Query("SELECT COUNT(*) FROM combat_logs WHERE isCriticalHit = 1 AND rolledAt BETWEEN :start AND :end")
    suspend fun countCritHitsBetween(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM combat_logs WHERE isCriticalMiss = 1 AND rolledAt BETWEEN :start AND :end")
    suspend fun countCritMissesBetween(start: Long, end: Long): Int

    // 전체 기간 D20 주사위 분포 (1~20)
    @Query("""
        SELECT d20Result, COUNT(*) as count
        FROM combat_logs
        GROUP BY d20Result
        ORDER BY d20Result
    """)
    suspend fun getD20Distribution(): List<D20Count>

    // 최근 N일 일별 XP 합계 (SQLite DATE 함수로 로컬 날짜 그룹핑)
    @Query("""
        SELECT DATE(rolledAt / 1000, 'unixepoch', 'localtime') as day,
               COALESCE(SUM(xpGained), 0) as xp
        FROM combat_logs
        WHERE rolledAt >= :sinceMills
        GROUP BY day
        ORDER BY day
    """)
    suspend fun getDailyXpSince(sinceMills: Long): List<DayXp>
}
