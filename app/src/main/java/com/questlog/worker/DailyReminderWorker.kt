package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.domain.repository.TaskRepository
import com.questlog.core.notification.NotificationChannels
import com.questlog.core.notification.NotificationHelper
import com.questlog.core.notification.NotificationIds
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

// 매일 08:00 실행. 활성 퀘스트 수를 확인하고 퀘스트가 있을 때만 알림 발송.
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_reminder"
    }

    override suspend fun doWork(): Result {
        val activeCount = runCatching {
            taskRepository.observeActive().first().size
        }.getOrElse { e ->
            Timber.e(e, "DailyReminderWorker: 활성 퀘스트 조회 실패")
            return Result.failure()
        }

        if (activeCount == 0) return Result.success()

        NotificationHelper.send(
            context = context,
            notificationId = NotificationIds.DAILY,
            channelId = NotificationChannels.DAILY,
            title = "오늘의 모험이 기다립니다!",
            body = "활성 퀘스트 ${activeCount}개가 남아 있습니다. 던전으로 출발!",
        )
        return Result.success()
    }
}
