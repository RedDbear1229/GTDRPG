package com.questlog.core.domain.repository

interface BuffRepository {
    suspend fun getActiveBuff(): String?
    suspend fun setActiveBuff(code: String)
    suspend fun clear()
}
