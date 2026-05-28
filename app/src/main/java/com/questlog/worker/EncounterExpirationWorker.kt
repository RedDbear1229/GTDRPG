package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.domain.repository.EncounterRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

// 15분 주기 — 만료된 PENDING 인카운터를 EXPIRED로 전환.
@HiltWorker
class EncounterExpirationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val encounterRepository: EncounterRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val expired = encounterRepository.expireOld()
        if (expired > 0) Timber.d("EncounterExpirationWorker: expired $expired encounters")
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "encounter_expiration_worker"
    }
}
