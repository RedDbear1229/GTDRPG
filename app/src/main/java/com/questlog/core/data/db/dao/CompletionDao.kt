package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.CombatLogEntity

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
