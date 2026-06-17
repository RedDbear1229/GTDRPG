package com.questlog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.domain.model.SyncResult
import com.questlog.core.domain.repository.DriveBackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class DriveAutoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val driveBackupRepository: DriveBackupRepository,
    private val appSettings: AppSettings,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "drive_auto_sync"
    }

    override suspend fun doWork(): Result {
        if (!appSettings.driveAutoSyncEnabled.first()) return Result.success()

        return when (val result = driveBackupRepository.uploadBackup()) {
            is SyncResult.Success -> {
                Timber.d("Drive 자동 백업 완료")
                Result.success()
            }
            is SyncResult.NotSignedIn -> {
                Timber.d("Drive 자동 백업 건너뜀: 미로그인")
                Result.success()
            }
            is SyncResult.NoBackupFound -> Result.success()
            is SyncResult.Error -> {
                Timber.w("Drive 자동 백업 실패: ${result.message}")
                Result.retry()
            }
        }
    }
}
