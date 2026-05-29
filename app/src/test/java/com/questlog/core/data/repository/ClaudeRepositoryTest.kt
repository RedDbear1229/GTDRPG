package com.questlog.core.data.repository

import com.questlog.core.data.budget.ApiBudget
import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.data.remote.ClaudeApiService
import com.questlog.core.data.remote.dto.ClaudeMessageResponse
import com.questlog.core.data.remote.dto.ContentBlock
import com.questlog.core.data.sanitizer.PromptSanitizer
import com.questlog.core.data.sanitizer.SanitizedTask
import com.questlog.core.data.secure.SecureStorage
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CombatResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClaudeRepositoryTest {

    private val api = mockk<ClaudeApiService>(relaxed = true)
    private val consentManager = mockk<ConsentManager>()
    private val apiBudget = mockk<ApiBudget>()
    private val secureStorage = mockk<SecureStorage>()
    private val sanitizer = PromptSanitizer()

    private lateinit var repo: ClaudeRepositoryImpl

    private val character = Character(
        name = "테스트",
        classType = CharacterClass.FIGHTER,
        maxHp = 20, currentHp = 20,
        strength = 16, dexterity = 12, constitution = 14,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private val task = SanitizedTask(
        title = "보고서 작성",
        context = "@사무실",
        createdAt = 1_000L,
        completedAt = 2_000L,
        challengeRating = 2f,
    )

    @BeforeEach
    fun setUp() {
        repo = ClaudeRepositoryImpl(api, consentManager, apiBudget, secureStorage, sanitizer)
    }

    @Test
    fun `canCallApi false → API 호출 없이 폴백 반환`() = runTest {
        coEvery { consentManager.canCallApi() } returns false

        val result = repo.generateCombatNarrative(character, task, CombatResult.Hit(15, 18, 14, 100))

        assertNotNull(result)
        assertTrue(result.isNotBlank())
        coVerify(exactly = 0) { api.generateMessage(any(), any(), any()) }
    }

    @Test
    fun `API 키 없음 → API 호출 없이 폴백 반환`() = runTest {
        coEvery { consentManager.canCallApi() } returns true
        coEvery { apiBudget.tryReserve() } returns true
        coEvery { secureStorage.getApiKey() } returns null

        val result = repo.generateCombatNarrative(character, task, CombatResult.Hit(10, 14, 14, 50))

        assertNotNull(result)
        coVerify(exactly = 0) { api.generateMessage(any(), any(), any()) }
    }

    @Test
    fun `일일 예산 초과 — tryReserve false → API 호출 없이 폴백 반환`() = runTest {
        coEvery { consentManager.canCallApi() } returns true
        coEvery { apiBudget.tryReserve() } returns false

        val result = repo.generateCombatNarrative(character, task, CombatResult.Hit(10, 14, 14, 50))

        assertNotNull(result)
        coVerify(exactly = 0) { api.generateMessage(any(), any(), any()) }
    }

    @Test
    fun `API 성공 → 응답 텍스트 반환`() = runTest {
        val expectedText = "전설적인 일격!"
        coEvery { consentManager.canCallApi() } returns true
        coEvery { apiBudget.tryReserve() } returns true
        coEvery { secureStorage.getApiKey() } returns "sk-test"
        coEvery { api.generateMessage(any(), any(), any()) } returns ClaudeMessageResponse(
            id = "msg_001",
            content = listOf(ContentBlock(type = "text", text = expectedText)),
            model = "claude-sonnet-4-6",
        )

        val result = repo.generateCombatNarrative(character, task, CombatResult.Hit(15, 18, 14, 100))

        assertTrue(result == expectedText)
    }

    @Test
    fun `API 예외 → 폴백 반환`() = runTest {
        coEvery { consentManager.canCallApi() } returns true
        coEvery { apiBudget.tryReserve() } returns true
        coEvery { secureStorage.getApiKey() } returns "sk-test"
        coEvery { api.generateMessage(any(), any(), any()) } throws RuntimeException("network error")

        val result = repo.generateCombatNarrative(character, task, CombatResult.Hit(15, 18, 14, 100))

        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `크리티컬 미스 — canCallApi false → 폴백 반환`() = runTest {
        coEvery { consentManager.canCallApi() } returns false

        val result = repo.generateCriticalMissNarrative(character, task, hpLost = 5)

        assertNotNull(result)
        assertTrue(result.isNotBlank())
        coVerify(exactly = 0) { api.generateMessage(any(), any(), any()) }
    }

    @Test
    fun `getClarifySuggestions — API 성공 → 줄 단위 파싱`() = runTest {
        val responseText = "- 이메일 초안 작성\n- 동료에게 검토 요청\n- 보고서 저장"
        coEvery { consentManager.canCallApi() } returns true
        coEvery { apiBudget.tryReserve() } returns true
        coEvery { secureStorage.getApiKey() } returns "sk-test"
        coEvery { api.generateMessage(any(), any(), any()) } returns ClaudeMessageResponse(
            id = "msg_002",
            content = listOf(ContentBlock(type = "text", text = responseText)),
            model = "claude-haiku-4-5-20251001",
        )

        val suggestions = repo.getClarifySuggestions("보고서", character)

        assertTrue(suggestions.size == 3)
        assertTrue(suggestions[0] == "이메일 초안 작성")
    }
}
