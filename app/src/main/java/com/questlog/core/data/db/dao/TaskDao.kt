package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.questlog.core.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

// 통계 쿼리용 DTO
data class LifeAreaCount(val lifeArea: String, val count: Int)
data class DayCount(val day: String, val count: Int)

// Phase 1 surface — F3.1 CombatDao/CompletionDao 가 완료 원자성을 인계받기 전까지의 임시 쿼리.
// 완료 가드 (`WHERE status='ACTIVE'`)는 F3.1 CompletionDao 트랜잭션에서 강제.
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET status = 'DELETED', deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE status = 'INBOX' ORDER BY createdAt ASC")
    fun getInboxItems(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE status = 'ACTIVE'
          AND (scheduledDate IS NULL OR scheduledDate <= :today)
        ORDER BY
            CASE WHEN dueDate IS NOT NULL THEN dueDate ELSE 9999999999999 END ASC,
            challengeRating DESC
    """)
    fun getActiveTasks(today: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND status != 'DELETED' ORDER BY createdAt ASC")
    fun getTasksByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'WAITING' ORDER BY delegatedAt ASC")
    fun getWaitingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'SOMEDAY' ORDER BY updatedAt DESC")
    fun getSomedayTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'INBOX'")
    fun getInboxCount(): Flow<Int>

    @Query("""
        SELECT * FROM tasks
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
          AND status != 'DELETED'
        ORDER BY updatedAt DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE status = 'ACTIVE'
          AND dueDate IS NOT NULL
          AND dueDate <= :todayEnd
        ORDER BY dueDate ASC
    """)
    fun getTasksDueToday(todayEnd: Long): Flow<List<TaskEntity>>

    // F1.5 Journal — 완료된 Task (DONE 상태 + completedAt 비어있지 않음). 시간 역순.
    // Phase 1 시점에는 Clarify 의 2분 룰만 DONE 상태를 만들지만,
    // F3.1 D20 전투 완료 후에는 이 쿼리가 모든 정상 완료 흐름을 노출한다.
    @Query("""
        SELECT * FROM tasks
        WHERE status = 'DONE'
          AND completedAt IS NOT NULL
        ORDER BY completedAt DESC
    """)
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("""
        SELECT COUNT(*) FROM tasks
        WHERE status = 'DONE'
          AND completedAt IS NOT NULL
          AND completedAt BETWEEN :startMillis AND :endMillis
    """)
    fun countCompletedBetween(startMillis: Long, endMillis: Long): Flow<Int>

    // F5.4 통계: 생활 영역별 완료 퀘스트 수
    @Query("""
        SELECT lifeArea, COUNT(*) as count
        FROM tasks
        WHERE status = 'DONE' AND completedAt IS NOT NULL
        GROUP BY lifeArea
        ORDER BY count DESC
    """)
    suspend fun getCompletedByLifeArea(): List<LifeAreaCount>

    // F5.4 통계: 최근 N일 일별 완료 퀘스트 수
    @Query("""
        SELECT DATE(completedAt / 1000, 'unixepoch', 'localtime') as day,
               COUNT(*) as count
        FROM tasks
        WHERE status = 'DONE'
          AND completedAt IS NOT NULL
          AND completedAt >= :sinceMills
        GROUP BY day
        ORDER BY day
    """)
    suspend fun getCompletedDailySince(sinceMills: Long): List<DayCount>

    // F5.4 통계: 스트릭 캘린더 — 완료된 날짜 목록 (최근 28일)
    @Query("""
        SELECT DISTINCT DATE(completedAt / 1000, 'unixepoch', 'localtime') as day
        FROM tasks
        WHERE status = 'DONE'
          AND completedAt IS NOT NULL
          AND completedAt >= :sinceMills
        ORDER BY day
    """)
    suspend fun getCompletedDatesSince(sinceMills: Long): List<String>
}
