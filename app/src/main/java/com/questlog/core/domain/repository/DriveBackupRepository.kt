package com.questlog.core.domain.repository

import com.questlog.core.domain.model.DriveAccount
import com.questlog.core.domain.model.SyncResult

interface DriveBackupRepository {
    fun getSignedInAccount(): DriveAccount?
    suspend fun uploadBackup(): SyncResult
    suspend fun downloadLatestBackup(): SyncResult
    suspend fun signOut()
}
