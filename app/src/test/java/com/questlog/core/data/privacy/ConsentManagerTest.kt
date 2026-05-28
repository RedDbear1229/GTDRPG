package com.questlog.core.data.privacy

import com.questlog.core.data.datastore.AppSettings
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.data.db.entity.ConsentRecordEntity
import com.questlog.core.domain.model.ConsentScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConsentManagerTest {

    private val dao = mockk<ConsentRecordDao>(relaxUnitFun = true)
    private val policyVersions = PolicyVersionProvider()
    private val appSettings = mockk<AppSettings>()
    private lateinit var manager: ConsentManager

    @BeforeEach
    fun setUp() {
        manager = ConsentManager(dao, policyVersions, appSettings)
    }

    @Test
    fun `동의 기록 없을 때 isGranted = false`() = runTest {
        coEvery { dao.latestActive(any()) } returns null
        assertFalse(manager.isGranted(ConsentScope.AI_OUTBOUND))
    }

    @Test
    fun `현재 정책 버전 일치하면 isGranted = true`() = runTest {
        val currentVersion = policyVersions.forScope(ConsentScope.AI_OUTBOUND)
        coEvery { dao.latestActive(ConsentScope.AI_OUTBOUND.name) } returns ConsentRecordEntity(
            scope = ConsentScope.AI_OUTBOUND.name,
            policyVersion = currentVersion,
            acceptedAt = System.currentTimeMillis(),
        )
        assertTrue(manager.isGranted(ConsentScope.AI_OUTBOUND))
    }

    @Test
    fun `정책 버전 불일치(구버전 동의)시 isGranted = false`() = runTest {
        coEvery { dao.latestActive(ConsentScope.AI_OUTBOUND.name) } returns ConsentRecordEntity(
            scope = ConsentScope.AI_OUTBOUND.name,
            policyVersion = 0, // 구버전
            acceptedAt = System.currentTimeMillis(),
        )
        assertFalse(manager.isGranted(ConsentScope.AI_OUTBOUND))
    }

    @Test
    fun `revoke 후 isGranted = false`() = runTest {
        coEvery { dao.latestActive(any()) } returns null
        coEvery { dao.revoke(any(), any()) } returns 1
        manager.revoke(ConsentScope.AI_OUTBOUND)
        assertFalse(manager.isGranted(ConsentScope.AI_OUTBOUND))
    }

    @Test
    fun `canCallApi — 동의 없으면 false (claudeApiEnabled 무관)`() = runTest {
        coEvery { dao.latestActive(any()) } returns null
        coEvery { appSettings.claudeApiEnabled } returns flowOf(true)
        assertFalse(manager.canCallApi())
    }

    @Test
    fun `canCallApi — 동의 있어도 claudeApiEnabled false면 false`() = runTest {
        val ver = policyVersions.forScope(ConsentScope.AI_OUTBOUND)
        coEvery { dao.latestActive(ConsentScope.AI_OUTBOUND.name) } returns ConsentRecordEntity(
            scope = ConsentScope.AI_OUTBOUND.name,
            policyVersion = ver,
            acceptedAt = System.currentTimeMillis(),
        )
        coEvery { appSettings.claudeApiEnabled } returns flowOf(false)
        assertFalse(manager.canCallApi())
    }

    @Test
    fun `canCallApi — 동의 + enabled = true면 true`() = runTest {
        val ver = policyVersions.forScope(ConsentScope.AI_OUTBOUND)
        coEvery { dao.latestActive(ConsentScope.AI_OUTBOUND.name) } returns ConsentRecordEntity(
            scope = ConsentScope.AI_OUTBOUND.name,
            policyVersion = ver,
            acceptedAt = System.currentTimeMillis(),
        )
        coEvery { appSettings.claudeApiEnabled } returns flowOf(true)
        assertTrue(manager.canCallApi())
    }

    @Test
    fun `grant 호출 시 DAO insert 발생`() = runTest {
        coEvery { dao.grant(any()) } returns 1L
        manager.grant(ConsentScope.AI_OUTBOUND)
        coVerify(exactly = 1) { dao.grant(match { it.scope == ConsentScope.AI_OUTBOUND.name }) }
    }
}
