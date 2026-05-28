package com.questlog.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    private const val HP_RESET_WORK = "hp_reset_and_streak"

    // MainActivity.onCreate / onNewIntent 에서 호출.
    // KEEP 정책: 이미 큐에 있으면 교체하지 않음 (재설치·업그레이드 후 중복 방지).
    fun schedule(context: Context) {
        val wm = WorkManager.getInstance(context)

        val hpResetRequest = PeriodicWorkRequestBuilder<HpResetAndStreakWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilMidnight(), TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(HP_RESET_WORK, ExistingPeriodicWorkPolicy.KEEP, hpResetRequest)

        val encounterRequest = PeriodicWorkRequestBuilder<RandomEncounterWorker>(12, TimeUnit.HOURS)
            .build()
        wm.enqueueUniquePeriodicWork(
            RandomEncounterWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            encounterRequest,
        )

        val expirationRequest = PeriodicWorkRequestBuilder<EncounterExpirationWorker>(15, TimeUnit.MINUTES)
            .build()
        wm.enqueueUniquePeriodicWork(
            EncounterExpirationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            expirationRequest,
        )

        // 주간 리뷰 알림: 매일 10:00 실행, 토요일에만 실제 알림 (Worker 내부 가드).
        val weeklyReviewRequest = PeriodicWorkRequestBuilder<WeeklyReviewReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntil10am(), TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(
            WeeklyReviewReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyReviewRequest,
        )
    }

    private fun millisUntilMidnight(): Long {
        val zone = ZoneId.systemDefault()
        val midnight = LocalDate.now(zone)
            .plusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        return (midnight - Instant.now().toEpochMilli()).coerceAtLeast(0L)
    }

    private fun millisUntil10am(): Long {
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val target10am = LocalDate.now(zone).atTime(LocalTime.of(10, 0)).atZone(zone).toInstant()
        val next10am = if (now.isBefore(target10am)) target10am
        else LocalDate.now(zone).plusDays(1).atTime(LocalTime.of(10, 0)).atZone(zone).toInstant()
        return (next10am.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(0L)
    }
}
