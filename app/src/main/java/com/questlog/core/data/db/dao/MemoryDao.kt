package com.questlog.core.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.questlog.core.data.db.entity.MemoryEntryEntity

@Dao
interface MemoryDao {

    // ABORT: 동일 날짜 중복 시 SQLiteConstraintException 발생 → MemoryRepositoryImpl 에서 AlreadyExists 로 변환.
    // REPLACE/UPSERT 금지 — 기존 메모 손실 위험 (CLAUDE.md 제약).
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entity: MemoryEntryEntity)

    @Query("SELECT * FROM memory_entries WHERE entryDate = :date LIMIT 1")
    suspend fun getByDate(date: String): MemoryEntryEntity?

    // Paging3: 최신 날짜 순 (DESC)
    @Query("SELECT * FROM memory_entries ORDER BY entryDate DESC")
    fun pageHistory(): PagingSource<Int, MemoryEntryEntity>

    @Query("SELECT COUNT(*) FROM memory_entries WHERE entryDate >= :weekStart")
    suspend fun countThisWeek(weekStart: String): Int

    @Query("SELECT * FROM memory_entries WHERE entryDate >= :weekStart ORDER BY entryDate ASC")
    suspend fun getThisWeek(weekStart: String): List<MemoryEntryEntity>

    @Query("UPDATE memory_entries SET enrichedBody = :enriched WHERE id = :id")
    suspend fun updateEnrichedBody(id: String, enriched: String)
}
