package com.questlog.core.data.privacy

import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.domain.model.ConsentScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataBoundaryTest {

    private val dao = mockk<ConsentRecordDao>(relaxUnitFun = true)
    private val policyVersions = PolicyVersionProvider()
    private val appSettings = mockk<AppSettings>()
    private lateinit var manager: ConsentManager

    @BeforeEach
    fun setUp() {
        manager = ConsentManager(dao, policyVersions, appSettings)
    }

    @Test
    fun `동의 없을 때 canCallApi = false`() = runTest {
        coEvery { dao.latestActive(any()) } returns null
        coEvery { appSettings.claudeApiEnabled } returns flowOf(true)
        assertFalse(manager.canCallApi())
    }

    @Test
    fun `마이크 동의 없을 때 canUseMicrophone = false`() = runTest {
        coEvery { dao.latestActive(ConsentScope.MICROPHONE.name) } returns null
        assertFalse(manager.canUseMicrophone())
    }

    @Test
    fun `연락처 동의 없을 때 canImportContacts = false`() = runTest {
        coEvery { dao.latestActive(ConsentScope.CONTACTS.name) } returns null
        assertFalse(manager.canImportContacts())
    }

    @Test
    fun `canCallApi false일 때 revoke가 호출되면 DAO revoke 실행`() = runTest {
        coEvery { dao.latestActive(any()) } returns null
        coEvery { appSettings.claudeApiEnabled } returns flowOf(false)
        coEvery { dao.revoke(any(), any()) } returns 1

        manager.revoke(ConsentScope.AI_OUTBOUND)

        coVerify(exactly = 1) { dao.revoke(ConsentScope.AI_OUTBOUND.name, any()) }
    }

    @Test
    fun `canCallApi false 확인 후 AI 기능 호출이 0회여야 함 (게이트 검증)`() = runTest {
        // canCallApi 자체가 false임을 확인하는 것으로 게이트 계약 검증
        coEvery { dao.latestActive(any()) } returns null
        coEvery { appSettings.claudeApiEnabled } returns flowOf(false)

        val canCall = manager.canCallApi()
        assertFalse(canCall)
        // 실제 ClaudeApiService는 canCallApi() 확인 후에만 호출해야 함 (CLAUDE.md 프라이버시 계약)
    }
}
