package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.usecase.UpdateStreakUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

// 자정 자동 실행 — Long Rest (maxHp 전체 회복) + 스트릭 계산.
// WorkManager Doze 지연 허용: 개인 앱이므로 ±15분 드리프트는 UX 무해.
// 정확한 자정이 중요하면 AlarmManager(setExactAndAllowWhileIdle) 로 교체 검토.
@HiltWorker
class HpResetAndStreakWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val characterRepository: CharacterRepository,
    private val updateStreak: UpdateStreakUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val character = characterRepository.getActive() ?: return Result.success()
        val streakResult = updateStreak(character)

        // Long Rest: 자정마다 HP 전체 회복 + 클래스 리소스 완충
        val restored = streakResult.character.copy(
            currentHp = streakResult.character.maxHp,
            classResourceCurrent = streakResult.character.classResourceMax,
            classResourceLastRefresh = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        characterRepository.upsert(restored)

        Timber.d(
            "HpResetAndStreakWorker done — streak=%d, longestStreak=%d, milestone=%s",
            restored.streakDays,
            restored.longestStreak,
            streakResult.milestone,
        )
        return Result.success()
    }
}
