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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

// 매일 09:00 실행. 오늘 마감인 퀘스트가 있으면 HIGH 우선순위 알림.
@HiltWorker
class DeadlineReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "deadline_reminder"
    }

    override suspend fun doWork(): Result {
        val zone = ZoneId.systemDefault()
        val todayEnd = LocalDate.now(zone)
            .atTime(LocalTime.MAX)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val dueToday = runCatching {
            taskRepository.observeDueToday(todayEnd).first()
        }.getOrElse { e ->
            Timber.e(e, "DeadlineReminderWorker: 기한 조회 실패")
            return Result.failure()
        }

        if (dueToday.isEmpty()) return Result.success()

        val body = if (dueToday.size == 1) {
            "「${dueToday.first().title}」 오늘까지입니다!"
        } else {
            "오늘 마감 퀘스트 ${dueToday.size}개 — 「${dueToday.first().title}」 외 ${dueToday.size - 1}개"
        }

        NotificationHelper.send(
            context = context,
            notificationId = NotificationIds.DEADLINE,
            channelId = NotificationChannels.DEADLINE,
            title = "⚔️ 기한 임박! 오늘까지 완료해야 합니다",
            body = body,
        )
        return Result.success()
    }
}
