package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.questlog.core.data.db.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    // 싱글 유저 — 행이 여럿이면 가장 최근 갱신본을 활성으로 본다 (방어적).
    @Query("SELECT * FROM characters ORDER BY updatedAt DESC LIMIT 1")
    fun observeActive(): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getActive(): CharacterEntity?

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun delete(id: String)
}
