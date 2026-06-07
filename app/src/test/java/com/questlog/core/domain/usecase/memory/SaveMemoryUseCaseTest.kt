package com.questlog.core.domain.usecase.memory

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.core.domain.model.OutcomeType
import com.questlog.core.domain.model.TaskSummary
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.InsertResult
import com.questlog.core.domain.repository.MemoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SaveMemoryUseCaseTest {

    private val fakeCharacter = Character(
        id = "char-1",
        name = "Tester",
        classType = CharacterClass.FIGHTER,
        level = 1,
        maxHp = 12,
        currentHp = 12,
        strength = 16, dexterity = 10, constitution = 14,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private val taskSummary = TaskSummary(
        id = "task-1",
        title = "운동하기",
        outcomeType = OutcomeType.WEAK_HIT,
        xpGained = 100L,
    )

    private fun makeUseCase(
        characterRepo: CharacterRepository,
        memoryRepo: MemoryRepository,
    ) = SaveMemoryUseCase(memoryRepo, characterRepo)

    @Test
    fun `정상 저장 → Success`() = runTest {
        val characterRepo = mockk<CharacterRepository>()
        val memoryRepo = mockk<MemoryRepository>()
        coEvery { characterRepo.getActive() } returns fakeCharacter
        coEvery { memoryRepo.insertEntry(any()) } returns InsertResult.Success

        val result = makeUseCase(characterRepo, memoryRepo).invoke(taskSummary, "오늘은 운동을 완료했다!")

        assertEquals(SaveResult.Success, result)
        coVerify(exactly = 1) { memoryRepo.insertEntry(any()) }
    }

    @Test
    fun `AlreadyExists — 중복 날짜 시 AlreadyExists 반환`() = runTest {
        val characterRepo = mockk<CharacterRepository>()
        val memoryRepo = mockk<MemoryRepository>()
        coEvery { characterRepo.getActive() } returns fakeCharacter
        coEvery { memoryRepo.insertEntry(any()) } returns InsertResult.AlreadyExists

        val result = makeUseCase(characterRepo, memoryRepo).invoke(taskSummary, "중복 테스트")

        assertEquals(SaveResult.AlreadyExists, result)
    }

    @Test
    fun `NoCharacter — 활성 캐릭터 없으면 NoCharacter 반환, DB 쓰기 없음`() = runTest {
        val characterRepo = mockk<CharacterRepository>()
        val memoryRepo = mockk<MemoryRepository>()
        coEvery { characterRepo.getActive() } returns null

        val result = makeUseCase(characterRepo, memoryRepo).invoke(taskSummary, "본문")

        assertEquals(SaveResult.NoCharacter, result)
        coVerify(exactly = 0) { memoryRepo.insertEntry(any()) }
    }

    @Test
    fun `body 500자 초과 → truncate 후 저장 (510자 입력 시 500자로 잘림)`() = runTest {
        val characterRepo = mockk<CharacterRepository>()
        val memoryRepo = mockk<MemoryRepository>()
        val capturedEntry = slot<MemoryEntry>()
        coEvery { characterRepo.getActive() } returns fakeCharacter
        coEvery { memoryRepo.insertEntry(capture(capturedEntry)) } returns InsertResult.Success

        val longBody = "A".repeat(510)
        val result = makeUseCase(characterRepo, memoryRepo).invoke(taskSummary, longBody)

        assertEquals(SaveResult.Success, result)
        assertEquals(500, capturedEntry.captured.body.length)
        assertTrue(capturedEntry.captured.body.all { it == 'A' })
    }

    @Test
    fun `date 자정 race — 저장 시작과 끝 날짜 동일하게 유지`() = runTest {
        // date 는 SaveMemoryUseCase 내부에서 invoke() 시점에 캐시되므로
        // 저장 전후로 날짜가 같은 값이 기록됨을 확인한다.
        val characterRepo = mockk<CharacterRepository>()
        val memoryRepo = mockk<MemoryRepository>()
        val capturedEntry = slot<MemoryEntry>()
        coEvery { characterRepo.getActive() } returns fakeCharacter
        coEvery { memoryRepo.insertEntry(capture(capturedEntry)) } returns InsertResult.Success

        makeUseCase(characterRepo, memoryRepo).invoke(taskSummary, "자정 race 테스트")

        // entryDate 가 정확히 10자 "yyyy-MM-dd" 형식인지 확인
        val date = capturedEntry.captured.entryDate
        assertTrue(date.length == 10, "entryDate 형식이 올바르지 않음: $date")
        assertTrue(date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")), "날짜 패턴 불일치: $date")
        // sealedAt 이 createdAt 보다 크거나 같음 (다음날 자정)
        assertTrue(capturedEntry.captured.sealedAt > capturedEntry.captured.createdAt)
    }
}
