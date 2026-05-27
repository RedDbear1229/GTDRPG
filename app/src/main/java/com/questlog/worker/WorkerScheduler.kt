package com.questlog.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    private const val HP_RESET_WORK = "hp_reset_and_streak"

    // MainActivity.onCreate / onNewIntent 에서 호출.
    // KEEP 정책: 이미 큐에 있으면 교체하지 않음 (재설치·업그레이드 후 중복 방지).
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<HpResetAndStreakWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilMidnight(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HP_RESET_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
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
}
