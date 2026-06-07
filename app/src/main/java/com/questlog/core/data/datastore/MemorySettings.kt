package com.questlog.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Memory of the Day 알림 설정.
// 기본값: reminderEnabled=true, reminderHour=21, reminderMinute=0 (저녁 9시)
@Singleton
class MemorySettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val MEMORY_REMINDER_ENABLED = booleanPreferencesKey("memory_reminder_enabled")
        private val MEMORY_REMINDER_HOUR = intPreferencesKey("memory_reminder_hour")
        private val MEMORY_REMINDER_MINUTE = intPreferencesKey("memory_reminder_minute")
    }

    val reminderEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[MEMORY_REMINDER_ENABLED] ?: true
    }

    val reminderHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[MEMORY_REMINDER_HOUR] ?: 21
    }

    val reminderMinute: Flow<Int> = dataStore.data.map { prefs ->
        prefs[MEMORY_REMINDER_MINUTE] ?: 0
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[MEMORY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[MEMORY_REMINDER_HOUR] = hour
            prefs[MEMORY_REMINDER_MINUTE] = minute
        }
    }
}
