package com.questlog.core.data.repository

import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.domain.model.ConsentScope
import com.questlog.core.domain.repository.ConsentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentRepositoryImpl @Inject constructor(
    private val consentManager: ConsentManager,
) : ConsentRepository {
    override suspend fun isGranted(scope: ConsentScope): Boolean = consentManager.isGranted(scope)
    override suspend fun grant(scope: ConsentScope) = consentManager.grant(scope)
    override suspend fun revoke(scope: ConsentScope) = consentManager.revoke(scope)
}
