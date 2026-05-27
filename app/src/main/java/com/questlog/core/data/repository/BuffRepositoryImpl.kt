package com.questlog.core.data.repository

import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.domain.repository.BuffRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuffRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings,
) : BuffRepository {

    override suspend fun getActiveBuff(): String? = appSettings.getActiveBuffCode()

    override suspend fun setActiveBuff(code: String) = appSettings.setActiveBuffCode(code)

    override suspend fun clear() = appSettings.setActiveBuffCode(null)
}
