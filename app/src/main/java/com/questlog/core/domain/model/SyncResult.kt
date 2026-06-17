package com.questlog.core.domain.model

sealed class SyncResult {
    data class Success(val message: String = "") : SyncResult()
    data class NotSignedIn(val message: String = "Google 계정 연결이 필요합니다") : SyncResult()
    data object NoBackupFound : SyncResult()
    data class Error(val message: String) : SyncResult()
}
