package com.questlog.core.domain.repository

import com.questlog.core.domain.model.Task
import kotlinx.coroutines.flow.Flow

// Phase 1 surface only — `completeTask` / `CombatResult` 는 F3.1 CompletionRepository 에서 추가.
interface TaskRepository {
    fun observeInbox(): Flow<List<Task>>
    fun observeActive(): Flow<List<Task>>
    fun observeByProject(projectId: String): Flow<List<Task>>
    fun observeWaiting(): Flow<List<Task>>
    fun observeSomeday(): Flow<List<Task>>
    fun observeInboxCount(): Flow<Int>
    fun observeDueToday(todayEndMillis: Long): Flow<List<Task>>
    fun observeCompleted(): Flow<List<Task>>
    fun observeCompletedCount(startMillis: Long, endMillis: Long): Flow<Int>
    fun search(query: String): Flow<List<Task>>
    suspend fun getById(id: String): Task?
    suspend fun upsert(task: Task)
    suspend fun softDelete(id: String)
}
