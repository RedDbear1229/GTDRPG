package com.questlog.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.questlog.MainActivity
import timber.log.Timber

object NotificationChannels {
    const val DAILY = "daily_reminder"
    const val DEADLINE = "deadline"
    const val HP_CRISIS = "hp_crisis"
    const val STREAK = "streak_risk"
    const val WEEKLY_REVIEW = "weekly_review"
    const val MEMORY_REMINDER = "memory_reminder"
}

object NotificationIds {
    const val DAILY = 1001
    const val DEADLINE = 1002
    const val HP_CRISIS = 1003
    const val STREAK = 1004
    const val WEEKLY_REVIEW = 2001  // WeeklyReviewReminderWorker 와 동일
    const val MEMORY_REMINDER = 3001
}

object NotificationHelper {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(
                NotificationChannels.DAILY,
                "일일 퀘스트 알림",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "매일 오전 퀘스트 현황 알림" },
            NotificationChannel(
                NotificationChannels.DEADLINE,
                "기한 임박 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "오늘 마감 퀘스트 알림" },
            NotificationChannel(
                NotificationChannels.HP_CRISIS,
                "HP 위기 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "HP가 위험 수준으로 낮을 때 알림" },
            NotificationChannel(
                NotificationChannels.STREAK,
                "스트릭 위기 알림",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "오늘 아직 퀘스트를 완료하지 않았을 때 저녁 알림" },
            NotificationChannel(
                NotificationChannels.WEEKLY_REVIEW,
                "주간 리뷰",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "토요일 주간 리뷰 알림" },
            NotificationChannel(
                NotificationChannels.MEMORY_REMINDER,
                "오늘의 기억 알림",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "완료한 퀘스트가 있을 때 저녁 기억 작성 알림" },
        ).forEach { nm.createNotificationChannel(it) }
    }

    fun send(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        runCatching { NotificationManagerCompat.from(context).notify(notificationId, notification) }
            .onFailure { Timber.w(it, "알림 전송 실패: id=$notificationId") }
    }
}
