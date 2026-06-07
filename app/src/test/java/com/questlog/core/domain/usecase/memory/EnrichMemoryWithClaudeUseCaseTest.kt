package com.questlog.core.domain.usecase.memory

import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.data.remote.ClaudeApiService
import com.questlog.core.data.remote.dto.ClaudeMessageResponse
import com.questlog.core.data.remote.dto.ContentBlock
import com.questlog.core.data.secure.SecureStorage
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.core.domain.model.OutcomeType
import com.questlog.core.domain.repository.MemoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EnrichMemoryWithClaudeUseCaseTest {

    private val fakeEntry = MemoryEntry(
        id = "mem-1",
        entryDate = "2026-05-28",
        characterId = "char-1",
        taskId = "task-1",
        taskTitleSnapshot = "운동하기",
        outcomeType = OutcomeType.WEAK_HIT,
        body = "오늘 힘겹게 운동을 완료했다.",
        enrichedBody = null,
        createdAt = 1_000_000L,
        sealedAt = 2_000_000L,
    )

    private fun makeUseCase(
        consentManager: ConsentManager,
        api: ClaudeApiService,
        secureStorage: SecureStorage,
        memoryRepo: MemoryRepository,
    ) = EnrichMemoryWithClaudeUseCase(consentManager, api, secureStorage, memoryRepo)

    @Test
    fun `canCallApi false → enrichedBody 업데이트 없음, API 0회 호출`() = runTest {
        val consentManager = mockk<ConsentManager>()
        val api = mockk<ClaudeApiService>()
        val secureStorage = mockk<SecureStorage>()
        val memoryRepo = mockk<MemoryRepository>()
        coEvery { consentManager.canCallApi() } returns false

        val result = makeUseCase(consentManager, api, secureStorage, memoryRepo).invoke(fakeEntry)

        assertNull(result)
        coVerify(exactly = 0) { api.generateMessage(any(), any(), any()) }
        coVerify(exactly = 0) { memoryRepo.updateEnrichedBody(any(), any()) }
    }

    @Test
    fun `API 성공 → enrichedBody 업데이트 호출됨`() = runTest {
        val consentManager = mockk<ConsentManager>()
        val api = mockk<ClaudeApiService>()
        val secureStorage = mockk<SecureStorage>()
        val memoryRepo = mockk<MemoryRepository>()
        coEvery { consentManager.canCallApi() } returns true
        coEvery { secureStorage.getApiKey() } returns "test-key"
        coEvery { api.generateMessage(any(), any(), any()) } returns ClaudeMessageResponse(
            id = "msg-test",
            content = listOf(ContentBlock(type = "text", text = "윤색된 본문")),
            model = "claude-haiku-4-5-20251001",
        )
        coEvery { memoryRepo.updateEnrichedBody(any(), any()) } returns Unit

        val result = makeUseCase(consentManager, api, secureStorage, memoryRepo).invoke(fakeEntry)

        assertEquals("윤색된 본문", result)
        coVerify(exactly = 1) { memoryRepo.updateEnrichedBody("mem-1", "윤색된 본문") }
    }

    @Test
    fun `API 예외 → enrichedBody null (폴백), updateEnrichedBody 호출 없음`() = runTest {
        val consentManager = mockk<ConsentManager>()
        val api = mockk<ClaudeApiService>()
        val secureStorage = mockk<SecureStorage>()
        val memoryRepo = mockk<MemoryRepository>()
        coEvery { consentManager.canCallApi() } returns true
        coEvery { secureStorage.getApiKey() } returns "test-key"
        coEvery { api.generateMessage(any(), any(), any()) } throws RuntimeException("Network error")

        val result = makeUseCase(consentManager, api, secureStorage, memoryRepo).invoke(fakeEntry)

        assertNull(result)
        coVerify(exactly = 0) { memoryRepo.updateEnrichedBody(any(), any()) }
    }
}
