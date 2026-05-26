package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import javax.inject.Inject

class TodayBucketUseCase @Inject constructor() {
    operator fun invoke(tasks: List<Task>, todayEndMillis: Long): List<Task> =
        tasks.filter { task ->
            task.status == TaskStatus.ACTIVE && (
                (task.dueDate != null && task.dueDate <= todayEndMillis) ||
                    (task.scheduledDate != null && task.scheduledDate <= todayEndMillis)
                )
        }
}
