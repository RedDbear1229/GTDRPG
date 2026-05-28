package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CombatLogDao {
    @Query("SELECT COALESCE(SUM(xpGained), 0) FROM combat_logs WHERE rolledAt BETWEEN :start AND :end")
    suspend fun sumXpBetween(start: Long, end: Long): Long

    @Query("SELECT COUNT(*) FROM combat_logs WHERE isCriticalHit = 1 AND rolledAt BETWEEN :start AND :end")
    suspend fun countCritHitsBetween(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM combat_logs WHERE isCriticalMiss = 1 AND rolledAt BETWEEN :start AND :end")
    suspend fun countCritMissesBetween(start: Long, end: Long): Int
}
