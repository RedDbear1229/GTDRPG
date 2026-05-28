package com.questlog.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.MainActivity
import com.questlog.core.data.db.dao.WeeklyReviewDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@HiltWorker
class WeeklyReviewReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val weeklyReviewDao: WeeklyReviewDao,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "weekly_review_reminder"
        private const val CHANNEL_ID = "weekly_review"
        private const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        val today = LocalDate.now(ZoneId.systemDefault())
        // 매일 10:00에 실행되지만 토요일에만 알림 발송
        if (today.dayOfWeek != DayOfWeek.SATURDAY) return Result.success()

        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekStartIso = monday.toString()

        val alreadyDone = weeklyReviewDao.getByWeekStart(weekStartIso) != null
        if (alreadyDone) {
            Timber.d("WeeklyReviewReminderWorker: 이미 완료된 주 — 알림 생략")
            return Result.success()
        }

        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "주간 리뷰",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "토요일 주간 리뷰 알림" }
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_OPEN_WEEKLY_REVIEW, true)
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("이번 주 주간 리뷰를 완료하세요!")
            .setContentText("던전마스터가 이번 주 모험을 정리할 시간입니다.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        runCatching { nm.notify(NOTIFICATION_ID, notification) }
            .onFailure { Timber.w(it, "WeeklyReviewReminderWorker: 알림 전송 실패") }
    }
}

private const val EXTRA_OPEN_WEEKLY_REVIEW = "open_weekly_review"
