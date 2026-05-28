package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.questlog.core.data.db.entity.EncounterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EncounterLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(encounter: EncounterLogEntity): Long

    @Query("SELECT * FROM encounter_logs ORDER BY generatedAt DESC")
    fun getAll(): Flow<List<EncounterLogEntity>>

    @Query("SELECT * FROM encounter_logs WHERE status = 'PENDING' ORDER BY expiresAt ASC")
    fun getPending(): Flow<List<EncounterLogEntity>>

    @Query("SELECT COUNT(*) FROM encounter_logs WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM encounter_logs WHERE templateKey = :key AND status = 'PENDING'")
    suspend fun pendingCountForTemplate(key: String): Int

    // 만료 처리 — 단순 UPDATE, 트랜잭션 불필요 (상태 전이만, 보상 없음)
    @Query("UPDATE encounter_logs SET status = 'EXPIRED' WHERE status = 'PENDING' AND expiresAt <= :now")
    suspend fun expireOld(now: Long = System.currentTimeMillis()): Int

    @Query("SELECT COUNT(*) FROM encounter_logs WHERE status = 'PENDING' AND generatedAt >= :since")
    suspend fun pendingCountSince(since: Long): Int

    @Query("SELECT * FROM encounter_logs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EncounterLogEntity?
}
