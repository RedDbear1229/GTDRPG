package com.questlog.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    private const val HP_RESET_WORK = "hp_reset_and_streak"

    // MainActivity.onCreate / onNewIntent 에서 호출.
    // KEEP 정책: 이미 큐에 있으면 교체하지 않음 (재설치·업그레이드 후 중복 방지).
    fun schedule(context: Context) {
        val wm = WorkManager.getInstance(context)

        // 자정 HP/스트릭 리셋
        wm.enqueueUniquePeriodicWork(
            HP_RESET_WORK, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<HpResetAndStreakWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(0, 0), TimeUnit.MILLISECONDS)
                .build(),
        )

        // 랜덤 인카운터 생성 (12시간 주기)
        wm.enqueueUniquePeriodicWork(
            RandomEncounterWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RandomEncounterWorker>(12, TimeUnit.HOURS).build(),
        )

        // 인카운터 만료 처리 (15분 주기)
        wm.enqueueUniquePeriodicWork(
            EncounterExpirationWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<EncounterExpirationWorker>(15, TimeUnit.MINUTES).build(),
        )

        // 알림 워커 (F5.5)
        wm.enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(8, 0), TimeUnit.MILLISECONDS)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            DeadlineReminderWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DeadlineReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(9, 0), TimeUnit.MILLISECONDS)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            HpCrisisWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<HpCrisisWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(12, 0), TimeUnit.MILLISECONDS)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            StreakRiskWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<StreakRiskWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(21, 0), TimeUnit.MILLISECONDS)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            WeeklyReviewReminderWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WeeklyReviewReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(10, 0), TimeUnit.MILLISECONDS)
                .build(),
        )
        // F6.4 Memory of the Day 리마인더 (21:00)
        wm.enqueueUniquePeriodicWork(
            MemoryReminderWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<MemoryReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(21, 0), TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    // 다음 지정 시각(시:분)까지 남은 밀리초. 이미 지났으면 내일 같은 시각.
    private fun millisUntil(hour: Int, minute: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val target = LocalDate.now(zone).atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant()
        val next = if (now.isBefore(target)) target
        else LocalDate.now(zone).plusDays(1).atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant()
        return (next.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(0L)
    }
}
