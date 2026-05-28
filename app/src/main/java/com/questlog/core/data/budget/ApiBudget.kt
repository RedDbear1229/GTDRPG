package com.questlog.core.data.budget

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiBudget @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        const val FREE_DAILY_LIMIT = 3
        private val API_BUDGET_COUNT = intPreferencesKey("api_budget_count")
        private val API_BUDGET_DATE = stringPreferencesKey("api_budget_date")
    }

    suspend fun canCall(): Boolean {
        val prefs = dataStore.data.first()
        val today = LocalDate.now().toString()
        val savedDate = prefs[API_BUDGET_DATE]
        if (savedDate != today) return true
        return (prefs[API_BUDGET_COUNT] ?: 0) < FREE_DAILY_LIMIT
    }

    // 확인과 증가를 단일 edit 트랜잭션에서 수행 — 동시 호출 시 초과 소비 방지.
    // 성공하면 true (호출자는 API를 실행해도 됨), 한도 초과면 false.
    // API 호출 실패 여부와 무관하게 슬롯이 소비되지만, 경쟁 조건 제거가 우선.
    suspend fun tryReserve(): Boolean {
        var reserved = false
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            if (prefs[API_BUDGET_DATE] != today) {
                prefs[API_BUDGET_DATE] = today
                prefs[API_BUDGET_COUNT] = 1
                reserved = true
            } else {
                val count = prefs[API_BUDGET_COUNT] ?: 0
                if (count < FREE_DAILY_LIMIT) {
                    prefs[API_BUDGET_COUNT] = count + 1
                    reserved = true
                }
            }
        }
        return reserved
    }

    suspend fun remainingToday(): Int {
        val prefs = dataStore.data.first()
        val today = LocalDate.now().toString()
        if (prefs[API_BUDGET_DATE] != today) return FREE_DAILY_LIMIT
        return (FREE_DAILY_LIMIT - (prefs[API_BUDGET_COUNT] ?: 0)).coerceAtLeast(0)
    }
}
