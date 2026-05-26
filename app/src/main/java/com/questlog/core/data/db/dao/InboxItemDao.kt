package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.questlog.core.data.db.entity.InboxItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InboxItemEntity)

    @Update
    suspend fun update(item: InboxItemEntity)

    @Query("SELECT * FROM inbox_items WHERE isClarified = 0 ORDER BY capturedAt ASC")
    fun getUnclarified(): Flow<List<InboxItemEntity>>

    @Query("SELECT COUNT(*) FROM inbox_items WHERE isClarified = 0")
    fun getUnclarifiedCount(): Flow<Int>

    @Query("SELECT * FROM inbox_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InboxItemEntity?

    @Query("DELETE FROM inbox_items WHERE id = :id")
    suspend fun delete(id: String)
}
