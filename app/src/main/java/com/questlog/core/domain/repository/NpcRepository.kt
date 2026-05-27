package com.questlog.core.domain.repository

import com.questlog.core.domain.model.Npc
import kotlinx.coroutines.flow.Flow

interface NpcRepository {
    fun getAll(): Flow<List<Npc>>
    suspend fun getById(id: String): Npc?
    suspend fun upsert(npc: Npc)
    suspend fun deleteById(id: String)
    suspend fun deleteImportedNpc(npcId: String)
    suspend fun clearContactData()
}
