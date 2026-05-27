package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.questlog.core.data.db.entity.NpcEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NpcDao {

    @Query("SELECT * FROM npcs ORDER BY name ASC")
    fun getAll(): Flow<List<NpcEntity>>

    @Query("SELECT * FROM npcs WHERE id = :id")
    suspend fun getById(id: String): NpcEntity?

    @Insert(onConflict = REPLACE)
    suspend fun upsert(npc: NpcEntity)

    @Query("DELETE FROM npcs WHERE id = :id")
    suspend fun deleteById(id: String)

    // WAITING 상태인 위임 태스크를 ACTIVE로 되돌린 후 NPC 삭제 (원자성)
    @Transaction
    suspend fun deleteImportedNpc(npcId: String) {
        clearDelegatedTasks(npcId)
        deleteById(npcId)
    }

    @Query("""
        UPDATE tasks
        SET status = 'WAITING',
            delegatedTo = NULL,
            delegatedAt = NULL,
            waitingFollowUpDate = NULL,
            updatedAt = :now
        WHERE delegatedTo = :npcId AND status = 'WAITING'
    """)
    suspend fun clearDelegatedTasks(npcId: String, now: Long = System.currentTimeMillis())

    // 연락처 PII 삭제 — PICKER 소스 NPC의 displayName, phoneNumber 제거
    @Query("""
        UPDATE npcs
        SET displayName = NULL,
            phoneNumber = NULL,
            updatedAt = :now
        WHERE source = 'PICKER'
    """)
    suspend fun clearContactData(now: Long = System.currentTimeMillis())
}
