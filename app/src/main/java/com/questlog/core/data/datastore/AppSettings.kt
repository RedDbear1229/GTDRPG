package com.questlog.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 운영 토글만 담는다.
// ⛔ contactsConsentGiven / aiConsentGiven 같은 동의 플래그 추가 금지 — ConsentRecordDao 단일 진실 공급원
@Singleton
class AppSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val CLAUDE_API_ENABLED = booleanPreferencesKey("claude_api_enabled")
        private val DRIVE_AUTO_SYNC_ENABLED = booleanPreferencesKey("drive_auto_sync_enabled")
        val ACTIVE_BUFF_CODE = stringPreferencesKey("active_buff_code")
    }

    val claudeApiEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CLAUDE_API_ENABLED] ?: false
    }

    val driveAutoSyncEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DRIVE_AUTO_SYNC_ENABLED] ?: false
    }

    suspend fun setDriveAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DRIVE_AUTO_SYNC_ENABLED] = enabled }
    }

    suspend fun setClaudeApiEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[CLAUDE_API_ENABLED] = enabled }
    }

    suspend fun getActiveBuffCode(): String? =
        dataStore.data.map { it[ACTIVE_BUFF_CODE] }.first()

    suspend fun setActiveBuffCode(code: String?) {
        dataStore.edit { prefs ->
            if (code != null) prefs[ACTIVE_BUFF_CODE] = code
            else prefs.remove(ACTIVE_BUFF_CODE)
        }
    }
}
