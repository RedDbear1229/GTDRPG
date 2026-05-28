package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.EncounterRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

// 12시간 주기 — PENDING 인카운터 1~2개 생성. 이미 동일 템플릿 PENDING 있으면 스킵 (EncounterRepository 내부 게이트).
@HiltWorker
class RandomEncounterWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val characterRepository: CharacterRepository,
    private val encounterRepository: EncounterRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val character = characterRepository.getActive() ?: return Result.success()
        // 24시간 내 이미 2개 이상이면 스킵 — 스팸 방지
        val since = System.currentTimeMillis() - 24L * 60 * 60 * 1000
        val recentCount = encounterRepository.pendingCountSince(since)
        if (recentCount >= MAX_PENDING_PER_DAY) {
            Timber.d("RandomEncounterWorker: skip — $recentCount pending encounters today")
            return Result.success()
        }

        val encounterCount = if (recentCount == 0) 2 else 1
        repeat(encounterCount) {
            val encounter = encounterRepository.generateEncounter(character.level)
            Timber.d("RandomEncounterWorker: generated ${encounter?.id}")
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "random_encounter_worker"
        private const val MAX_PENDING_PER_DAY = 2
    }
}
