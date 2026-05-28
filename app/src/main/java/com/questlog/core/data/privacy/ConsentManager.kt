package com.questlog.core.data.privacy

import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.data.db.entity.ConsentRecordEntity
import com.questlog.core.domain.model.ConsentScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    private val dao: ConsentRecordDao,
    private val policyVersions: PolicyVersionProvider,
    private val appSettings: AppSettings,
) {
    suspend fun isGranted(scope: ConsentScope): Boolean {
        val record = dao.latestActive(scope.name) ?: return false
        return record.policyVersion == policyVersions.forScope(scope)
    }

    // 동의 + 활성화 이중 확인 — CLAUDE.md 프라이버시 계약 §3
    suspend fun canCallApi(): Boolean =
        isGranted(ConsentScope.AI_OUTBOUND) && appSettings.claudeApiEnabled.first()

    suspend fun canUseMicrophone(): Boolean = isGranted(ConsentScope.MICROPHONE)

    suspend fun canImportContacts(): Boolean = isGranted(ConsentScope.CONTACTS)

    suspend fun grant(scope: ConsentScope) {
        dao.grant(
            ConsentRecordEntity(
                scope = scope.name,
                policyVersion = policyVersions.forScope(scope),
                acceptedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun revoke(scope: ConsentScope) {
        dao.revoke(scope.name, System.currentTimeMillis())
    }
}
