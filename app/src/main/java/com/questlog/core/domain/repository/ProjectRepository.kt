package com.questlog.core.domain.repository

import com.questlog.core.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeActive(): Flow<List<Project>>
    fun observeAll(): Flow<List<Project>>
    suspend fun getById(id: String): Project?
    suspend fun upsert(project: Project)
    suspend fun delete(id: String)
}
