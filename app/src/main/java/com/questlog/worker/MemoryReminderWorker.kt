package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.domain.usecase.memory.GetCandidateCompletionsUseCase
import com.questlog.core.notification.NotificationChannels
import com.questlog.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDate

// 매일 21:00 실행 (WorkerScheduler 등록).
// 오늘 완료 퀘스트 ≥ 1 AND 오늘 미작성 → "기억을 남겨보세요" 알림 발송.
@HiltWorker
class MemoryReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getCandidates: GetCandidateCompletionsUseCase,
    private val memoryRepository: com.questlog.core.domain.repository.MemoryRepository,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "memory_reminder"
        const val NOTIFICATION_ID = 3001
    }

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toString()

        val alreadyWritten = runCatching {
            memoryRepository.getTodayEntry(today) != null
        }.getOrElse { e ->
            Timber.e(e, "MemoryReminderWorker: 오늘 기억 조회 실패")
            return Result.failure()
        }

        if (alreadyWritten) return Result.success()

        val candidates = runCatching {
            getCandidates(today)
        }.getOrElse { e ->
            Timber.e(e, "MemoryReminderWorker: 완료 퀘스트 조회 실패")
            return Result.failure()
        }

        if (candidates.isEmpty()) return Result.success()

        NotificationHelper.send(
            context = context,
            notificationId = NOTIFICATION_ID,
            channelId = NotificationChannels.MEMORY_REMINDER,
            title = "오늘의 기억을 남겨보세요",
            body = "${candidates.size}개의 퀘스트를 완료했어요. 오늘의 이야기를 기록해두세요!",
        )
        return Result.success()
    }
}
