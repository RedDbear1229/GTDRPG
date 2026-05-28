package com.questlog.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.data.secure.SecureStorage
import com.questlog.core.domain.model.ConsentScope
import com.questlog.core.domain.repository.ConsentRepository
import com.questlog.core.domain.usecase.DeleteAiCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val aiConsentGranted: Boolean = false,
    val claudeApiEnabled: Boolean = false,
    val apiKeySet: Boolean = false,
    val pendingConsentScope: ConsentScope? = null,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val consentRepository: ConsentRepository,
    private val appSettings: AppSettings,
    private val secureStorage: SecureStorage,
    private val deleteAiCache: DeleteAiCacheUseCase,
) : ViewModel() {

    private val consentState = MutableStateFlow(false)
    private val extra = MutableStateFlow(Pair(false, false)) // (claudeEnabled, keySet)

    val uiState = combine(consentState, extra) { consent, (enabled, keySet) ->
        SettingsUiState(
            aiConsentGranted = consent,
            claudeApiEnabled = enabled,
            apiKeySet = keySet,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _pendingScope = MutableStateFlow<ConsentScope?>(null)

    init {
        viewModelScope.launch {
            consentState.value = consentRepository.isGranted(ConsentScope.AI_OUTBOUND)
            appSettings.claudeApiEnabled.collect { enabled ->
                extra.update { it.copy(first = enabled, second = secureStorage.getApiKey() != null) }
            }
        }
    }

    fun requestAiConsent() { _pendingScope.value = ConsentScope.AI_OUTBOUND }

    fun onConsentAccepted(scope: ConsentScope) {
        viewModelScope.launch {
            consentRepository.grant(scope)
            consentState.value = consentRepository.isGranted(scope)
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
            consentState.value = false
            appSettings.setClaudeApiEnabled(false)
        }
    }

    fun saveApiKey(key: String) {
        secureStorage.saveApiKey(key)
        extra.update { it.copy(second = true) }
    }

    fun clearError() = Unit
}
