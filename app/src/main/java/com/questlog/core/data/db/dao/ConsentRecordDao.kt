package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.questlog.core.data.db.entity.ConsentRecordEntity

@Dao
interface ConsentRecordDao {

    @Query(
        """
        SELECT * FROM consent_records
        WHERE scope = :scope AND revokedAt IS NULL
        ORDER BY acceptedAt DESC
        LIMIT 1
        """
    )
    suspend fun latestActive(scope: String): ConsentRecordEntity?

    @Insert
    suspend fun grant(record: ConsentRecordEntity): Long

    @Query(
        "UPDATE consent_records SET revokedAt = :now WHERE scope = :scope AND revokedAt IS NULL"
    )
    suspend fun revoke(scope: String, now: Long): Int
}
