package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.NpcDao
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.Npc
import com.questlog.core.domain.repository.NpcRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NpcRepositoryImpl @Inject constructor(
    private val dao: NpcDao,
) : NpcRepository {

    override fun getAll(): Flow<List<Npc>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Npc? = dao.getById(id)?.toDomain()

    override suspend fun upsert(npc: Npc) = dao.upsert(npc.toEntity())

    override suspend fun deleteById(id: String) = dao.deleteById(id)

    override suspend fun deleteImportedNpc(npcId: String) = dao.deleteImportedNpc(npcId)

    override suspend fun clearContactData() = dao.clearContactData()
}
