package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.Project
import com.questlog.core.domain.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val dao: ProjectDao,
) : ProjectRepository {

    override fun observeActive(): Flow<List<Project>> =
        dao.getActive().map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Project>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Project? =
        withContext(Dispatchers.IO) { dao.getById(id)?.toDomain() }

    override suspend fun upsert(project: Project) =
        withContext(Dispatchers.IO) {
            dao.insert(project.toEntity())
        }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) { dao.delete(id) }
}
