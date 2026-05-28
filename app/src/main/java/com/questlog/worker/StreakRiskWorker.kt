package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.notification.NotificationChannels
import com.questlog.core.notification.NotificationHelper
import com.questlog.core.notification.NotificationIds
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

// 매일 21:00 실행. streakDays ≥ 3이고 오늘 완료 퀘스트 0개면 스트릭 위기 알림.
// streak < 3이면 알림 생략 — 아직 잃을 스트릭이 없음.
@HiltWorker
class StreakRiskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val characterRepository: CharacterRepository,
    private val taskDao: TaskDao,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "streak_risk"
        private const val MIN_STREAK_TO_WARN = 3
    }

    override suspend fun doWork(): Result {
        val character = runCatching {
            characterRepository.getActive()
        }.getOrElse { e ->
            Timber.e(e, "StreakRiskWorker: 캐릭터 조회 실패")
            return Result.failure()
        } ?: return Result.success()

        if (character.streakDays < MIN_STREAK_TO_WARN) return Result.success()

        val zone = ZoneId.systemDefault()
        val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val todayEnd = LocalDate.now(zone).atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

        val completedToday = runCatching {
            taskDao.getCompletedDailySince(todayStart)
                .firstOrNull()?.count ?: 0
        }.getOrElse { e ->
            Timber.e(e, "StreakRiskWorker: 오늘 완료 조회 실패")
            return Result.failure()
        }

        if (completedToday > 0) return Result.success()

        NotificationHelper.send(
            context = context,
            notificationId = NotificationIds.STREAK,
            channelId = NotificationChannels.STREAK,
            title = "🔥 ${character.streakDays}일 스트릭이 위험합니다!",
            body = "오늘 아직 퀘스트를 완료하지 않았습니다. 하나만 완료해도 스트릭이 유지됩니다.",
        )
        return Result.success()
    }
}
