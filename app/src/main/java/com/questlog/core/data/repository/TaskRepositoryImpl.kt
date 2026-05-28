package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
) : TaskRepository {

    override fun observeInbox(): Flow<List<Task>> =
        dao.getInboxItems().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Task>> =
        dao.getActiveTasks().map { list -> list.map { it.toDomain() } }

    override fun observeByProject(projectId: String): Flow<List<Task>> =
        dao.getTasksByProject(projectId).map { list -> list.map { it.toDomain() } }

    override fun observeWaiting(): Flow<List<Task>> =
        dao.getWaitingTasks().map { list -> list.map { it.toDomain() } }

    override fun observeSomeday(): Flow<List<Task>> =
        dao.getSomedayTasks().map { list -> list.map { it.toDomain() } }

    override fun observeInboxCount(): Flow<Int> = dao.getInboxCount()

    override fun observeDueToday(todayEndMillis: Long): Flow<List<Task>> =
        dao.getTasksDueToday(todayEndMillis).map { list -> list.map { it.toDomain() } }

    override fun observeCompleted(): Flow<List<Task>> =
        dao.getCompletedTasks().map { list -> list.map { it.toDomain() } }

    override fun observeCompletedCount(startMillis: Long, endMillis: Long): Flow<Int> =
        dao.countCompletedBetween(startMillis, endMillis)

    override fun search(query: String): Flow<List<Task>> =
        dao.searchTasks(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Task? =
        withContext(Dispatchers.IO) { dao.getById(id)?.toDomain() }

    override suspend fun upsert(task: Task): Unit =
        withContext(Dispatchers.IO) {
            dao.insert(task.toEntity())
        }

    override suspend fun softDelete(id: String) =
        withContext(Dispatchers.IO) { dao.softDelete(id) }
}
