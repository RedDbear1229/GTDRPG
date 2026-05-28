package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.questlog.core.data.db.entity.WeeklyReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReviewDao {
    // IGNORE: 같은 주에 두 번 제출해도 첫 제출만 기록됨 (멱등성).
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: WeeklyReviewEntity): Long

    @Query("SELECT * FROM weekly_reviews WHERE weekStart = :weekStart LIMIT 1")
    suspend fun getByWeekStart(weekStart: String): WeeklyReviewEntity?

    @Query("SELECT * FROM weekly_reviews ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLatest(): WeeklyReviewEntity?

    @Query("SELECT * FROM weekly_reviews ORDER BY completedAt DESC LIMIT 10")
    fun observeRecent(): Flow<List<WeeklyReviewEntity>>
}
