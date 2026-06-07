package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.CombatLogEntity

// Memory of the Day: 오늘 완료 + 전투 로그 JOIN 쿼리용 DTO
data class CompletedTaskWithLog(
    val taskId: String,
    val taskTitle: String,
    val xpGained: Long,
    val d20Result: Int?,  // null = 전투 없이 완료 (QuickDone/2분룰)
)

// 원자성 계약 (CLAUDE.md §데이터 무결성):
//   commitCompletion() 이 단일 @Transaction 진입점. 분산 쓰기 금지.
@Dao
interface CompletionDao {

    @Query(
        "UPDATE tasks SET status='DONE', completedAt=:now, updatedAt=:now " +
            "WHERE id=:taskId AND status='ACTIVE'"
    )
    suspend fun markTaskDone(taskId: String, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: CombatLogEntity)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Query("SELECT COUNT(*) FROM combat_logs WHERE taskId=:taskId")
    suspend fun hasLog(taskId: String): Int

    // F6.1 Memory of the Day: 특정 날짜("yyyy-MM-dd") 에 완료된 Task + CombatLog JOIN
    // DATE(completedAt/1000,'unixepoch','localtime') 를 사용해 로컬 날짜로 비교.
    @Query("""
        SELECT t.id as taskId, t.title as taskTitle,
               COALESCE(cl.xpGained, 0) as xpGained,
               cl.d20Result as d20Result
        FROM tasks t
        LEFT JOIN combat_logs cl ON cl.taskId = t.id
        WHERE t.status = 'DONE'
          AND DATE(t.completedAt / 1000, 'unixepoch', 'localtime') = :date
        ORDER BY t.completedAt DESC
    """)
    suspend fun getCompletedWithLogByDate(date: String): List<CompletedTaskWithLog>

    // returns true = 커밋 성공, false = 이미 완료 (status guard 0 rows)
    @Transaction
    suspend fun commitCompletion(
        taskId: String,
        log: CombatLogEntity,
        updatedCharacter: CharacterEntity,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        if (markTaskDone(taskId, now) == 0) return false
        insertLog(log)
        updateCharacter(updatedCharacter)
        return true
    }
}
