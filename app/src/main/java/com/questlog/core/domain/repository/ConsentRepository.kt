package com.questlog.core.domain.repository

import com.questlog.core.domain.model.ConsentScope

interface ConsentRepository {
    suspend fun isGranted(scope: ConsentScope): Boolean
    suspend fun grant(scope: ConsentScope)
    suspend fun revoke(scope: ConsentScope)
}
