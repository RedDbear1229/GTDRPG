package com.questlog.core.domain.repository

import com.questlog.core.domain.model.ClaimResult
import com.questlog.core.domain.model.EncounterLog
import kotlinx.coroutines.flow.Flow

interface EncounterRepository {
    fun getPending(): Flow<List<EncounterLog>>
    fun getAll(): Flow<List<EncounterLog>>
    fun getPendingCount(): Flow<Int>
    suspend fun getById(id: String): EncounterLog?
    suspend fun pendingCountForTemplate(key: String): Int
    suspend fun pendingCountSince(since: Long): Int
    suspend fun generateEncounter(characterLevel: Int): EncounterLog?
    suspend fun claimReward(encounterId: String, characterId: String, rewardXp: Long): ClaimResult
    suspend fun expireOld(): Int
}
