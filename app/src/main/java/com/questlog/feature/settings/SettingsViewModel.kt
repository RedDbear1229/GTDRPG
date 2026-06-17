package com.questlog.feature.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.data.GoogleAuthManager
import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.data.secure.SecureStorage
import com.questlog.core.domain.model.ConsentScope
import com.questlog.core.domain.model.DriveAccount
import com.questlog.core.domain.model.SyncResult
import com.questlog.core.domain.repository.ConsentRepository
import com.questlog.core.domain.repository.DriveBackupRepository
import com.questlog.core.domain.usecase.DeleteAiCacheUseCase
import com.questlog.worker.WorkerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val aiConsentGranted: Boolean = false,
    val claudeApiEnabled: Boolean = false,
    val apiKeySet: Boolean = false,
    val driveAccount: DriveAccount? = null,
    val driveAutoSyncEnabled: Boolean = false,
    val syncStatus: String? = null,
    val isSyncing: Boolean = false,
    val needsRestart: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentRepository: ConsentRepository,
    private val appSettings: AppSettings,
    private val secureStorage: SecureStorage,
    private val deleteAiCache: DeleteAiCacheUseCase,
    private val driveBackupRepository: DriveBackupRepository,
    private val googleAuthManager: GoogleAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    aiConsentGranted = consentRepository.isGranted(ConsentScope.AI_OUTBOUND),
                    driveAccount = driveBackupRepository.getSignedInAccount(),
                )
            }
        }
        viewModelScope.launch {
            appSettings.claudeApiEnabled.collect { enabled ->
                _uiState.update { it.copy(claudeApiEnabled = enabled, apiKeySet = secureStorage.getApiKey() != null) }
            }
        }
        viewModelScope.launch {
            appSettings.driveAutoSyncEnabled.collect { autoSync ->
                _uiState.update { it.copy(driveAutoSyncEnabled = autoSync) }
            }
        }
    }

    // ── Claude AI ──────────────────────────────────────────────────────────

    fun onConsentAccepted(scope: ConsentScope) {
        viewModelScope.launch {
            consentRepository.grant(scope)
            _uiState.update { it.copy(aiConsentGranted = consentRepository.isGranted(scope)) }
        }
    }

    fun onConsentDeclined() { /* 아무것도 하지 않음 */ }

    fun toggleClaudeApi(enabled: Boolean) {
        viewModelScope.launch { appSettings.setClaudeApiEnabled(enabled) }
    }

    fun revokeAiConsent() {
        viewModelScope.launch {
            consentRepository.revoke(ConsentScope.AI_OUTBOUND)
            deleteAiCache()
            appSettings.setClaudeApiEnabled(false)
            _uiState.update { it.copy(aiConsentGranted = false) }
        }
    }

    fun saveApiKey(key: String) {
        secureStorage.saveApiKey(key)
        _uiState.update { it.copy(apiKeySet = true) }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    // ── Google Drive ───────────────────────────────────────────────────────

    fun getSignInIntent(): Intent = googleAuthManager.getSignInIntent()

    fun onSignInResult(data: Intent?) {
        viewModelScope.launch {
            val account = googleAuthManager.handleSignInResult(data)
            if (account != null) {
                _uiState.update {
                    it.copy(
                        driveAccount = googleAuthManager.toDriveAccount(account),
                        syncStatus = "${account.email} 연결됨",
                    )
                }
            } else {
                _uiState.update { it.copy(error = "Google 로그인에 실패했습니다") }
            }
        }
    }

    fun signOutDrive() {
        viewModelScope.launch {
            driveBackupRepository.signOut()
            appSettings.setDriveAutoSyncEnabled(false)
            WorkerScheduler.cancelDriveSync(context)
            _uiState.update { it.copy(driveAccount = null, driveAutoSyncEnabled = false, syncStatus = null) }
        }
    }

    fun uploadBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatus = null) }
            when (val result = driveBackupRepository.uploadBackup()) {
                is SyncResult.Success -> _uiState.update { it.copy(syncStatus = "백업 완료") }
                is SyncResult.NotSignedIn -> _uiState.update { it.copy(error = result.message) }
                is SyncResult.Error -> _uiState.update { it.copy(error = result.message) }
                else -> Unit
            }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun downloadBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatus = null) }
            when (val result = driveBackupRepository.downloadLatestBackup()) {
                is SyncResult.Success -> _uiState.update { it.copy(needsRestart = true) }
                is SyncResult.NotSignedIn -> _uiState.update { it.copy(error = result.message) }
                is SyncResult.NoBackupFound -> _uiState.update { it.copy(error = "백업 파일이 없습니다") }
                is SyncResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun toggleDriveAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            appSettings.setDriveAutoSyncEnabled(enabled)
            if (enabled) WorkerScheduler.scheduleDriveSync(context)
            else WorkerScheduler.cancelDriveSync(context)
        }
    }
}
