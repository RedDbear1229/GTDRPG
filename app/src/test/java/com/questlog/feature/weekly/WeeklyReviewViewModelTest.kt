package com.questlog.feature.weekly

import com.questlog.core.data.db.dao.CombatLogDao
import com.questlog.core.data.db.dao.WeeklyReviewDao
import com.questlog.core.data.db.entity.WeeklyReviewEntity
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.ClaudeRepository
import com.questlog.core.domain.repository.MemoryRepository
import com.questlog.core.domain.repository.TaskRepository
import com.questlog.core.domain.usecase.GainXPUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyReviewViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val weeklyReviewDao = mockk<WeeklyReviewDao>()
    private val combatLogDao = mockk<CombatLogDao>()
    private val taskRepository = mockk<TaskRepository>()
    private val characterRepository = mockk<CharacterRepository>()
    private val claudeRepository = mockk<ClaudeRepository>()
    private val memoryRepository = mockk<MemoryRepository>(relaxed = true)
    private val gainXP = mockk<GainXPUseCase>()

    private val character = Character(
        name = "테스트 영웅",
        classType = CharacterClass.FIGHTER,
        maxHp = 20, currentHp = 20,
        strength = 16, dexterity = 12, constitution = 14,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private fun createViewModel() = WeeklyReviewViewModel(
        weeklyReviewDao = weeklyReviewDao,
        combatLogDao = combatLogDao,
        taskRepository = taskRepository,
        characterRepository = characterRepository,
        claudeRepository = claudeRepository,
        gainXP = gainXP,
        memoryRepository = memoryRepository,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { weeklyReviewDao.getByWeekStart(any()) } returns null
        coEvery { combatLogDao.sumXpBetween(any(), any()) } returns 500L
        coEvery { combatLogDao.countCritHitsBetween(any(), any()) } returns 2
        coEvery { combatLogDao.countCritMissesBetween(any(), any()) } returns 1
        coEvery { taskRepository.observeCompletedCount(any(), any()) } returns flowOf(5)
        coEvery { taskRepository.observeActive() } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기화 시 주간 통계 로드됨`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(5, state.weekStats.completedCount)
        assertEquals(500L, state.weekStats.xpGained)
        assertEquals(2, state.weekStats.critCount)
        assertEquals(6, state.steps.size)
    }

    @Test
    fun `이미 완료된 주는 isAlreadyDone = true`() = runTest {
        coEvery { weeklyReviewDao.getByWeekStart(any()) } returns WeeklyReviewEntity(
            weekStart = "2026-05-25",
            weekLabel = "5월 4주차",
            completedCount = 3,
            xpGained = 300,
            critCount = 1,
            missCount = 0,
            unfinishedCount = 2,
        )
        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isAlreadyDone)
    }

    @Test
    fun `checkCurrentStep 후 현재 스텝 isChecked = true`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.steps[0].isChecked)
        vm.checkCurrentStep()
        assertTrue(vm.uiState.value.steps[0].isChecked)
    }

    @Test
    fun `nextStep — 확인 전에는 스텝 이동 안 됨 (checkCurrentStep 로 확인 필요)`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.nextStep()
        advanceUntilIdle()
        // checkCurrentStep 없이 nextStep 하면 아직 isChecked=false이므로 isComplete 미도달
        assertFalse(vm.uiState.value.isComplete)
    }

    @Test
    fun `completeReview — INSERT 성공 시 XP 200 지급 + isComplete = true`() = runTest {
        coEvery { weeklyReviewDao.insert(any()) } returns 1L
        coEvery { gainXP(WeeklyReviewEntity.REWARD_XP) } returns character.copy(totalXpEarned = 200)

        val vm = createViewModel()
        advanceUntilIdle()

        // 6스텝 모두 통과
        repeat(6) {
            vm.checkCurrentStep()
            vm.nextStep()
            advanceUntilIdle()
        }

        assertTrue(vm.uiState.value.isComplete)
        assertEquals(WeeklyReviewEntity.REWARD_XP, vm.uiState.value.xpAwarded)
        coVerify(exactly = 1) { gainXP(WeeklyReviewEntity.REWARD_XP) }
    }

    @Test
    fun `completeReview — INSERT IGNORE(-1) 시 XP 미지급`() = runTest {
        coEvery { weeklyReviewDao.insert(any()) } returns -1L

        val vm = createViewModel()
        advanceUntilIdle()

        repeat(6) {
            vm.checkCurrentStep()
            vm.nextStep()
            advanceUntilIdle()
        }

        assertTrue(vm.uiState.value.isComplete)
        assertEquals(0L, vm.uiState.value.xpAwarded)
        coVerify(exactly = 0) { gainXP(any()) }
    }

    @Test
    fun `generateAiSummary — Claude 성공 시 aiSummary 업데이트`() = runTest {
        coEvery { characterRepository.getActive() } returns character
        coEvery { claudeRepository.generateWeeklyReviewSummary(any(), any(), any(), any(), any(), any(), any()) } returns "용감한 전사여, 이번 주도 수고했다!"

        val vm = createViewModel()
        advanceUntilIdle()

        vm.generateAiSummary()
        advanceUntilIdle()

        assertEquals("용감한 전사여, 이번 주도 수고했다!", vm.uiState.value.aiSummary)
        assertFalse(vm.uiState.value.isGeneratingAi)
    }
}
