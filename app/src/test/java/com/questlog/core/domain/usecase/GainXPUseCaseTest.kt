package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GainXPUseCaseTest {

    private fun baseCharacter(level: Int = 1, totalXp: Long = 0L) = Character(
        name = "Tester",
        classType = CharacterClass.FIGHTER,
        level = level,
        currentXp = totalXp - XpThresholds.cumulativeForLevel(level),
        totalXpEarned = totalXp,
        maxHp = HpCalculator.maxHp(CharacterClass.FIGHTER, level, 10),
        currentHp = 10,
        strength = 10, dexterity = 10, constitution = 10,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = ProficiencyBonus.forLevel(level),
    )

    private fun fakeRepo(character: Character?): CharacterRepository = object : CharacterRepository {
        private var stored = character
        override fun observeActive(): Flow<Character?> = flowOf(stored)
        override suspend fun getActive(): Character? = stored
        override suspend fun getById(id: String): Character? = stored?.takeIf { it.id == id }
        override suspend fun upsert(c: Character) { stored = c }
        override suspend fun delete(id: String) { stored = null }
    }

    @Test
    fun `캐릭터 없으면 null 반환`() = runTest {
        val result = GainXPUseCase(fakeRepo(null)).invoke(100)
        assertNull(result)
    }

    @Test
    fun `XP 적립 후 totalXpEarned 누적`() = runTest {
        val character = baseCharacter()
        val result = GainXPUseCase(fakeRepo(character)).invoke(100)
        assertEquals(100L, result?.totalXpEarned)
    }

    @Test
    fun `레벨업 임계값(300 XP) 초과 시 레벨 2 진입`() = runTest {
        val character = baseCharacter()
        val result = GainXPUseCase(fakeRepo(character)).invoke(300)
        assertEquals(2, result?.level)
    }

    @Test
    fun `레벨 2 진입 시 currentXp 는 300 XP 기준 초과분`() = runTest {
        val character = baseCharacter()
        val result = GainXPUseCase(fakeRepo(character)).invoke(350)
        // lv2 threshold = 300, currentXp = 350 - 300 = 50
        assertEquals(50L, result?.currentXp)
    }

    @Test
    fun `이미 최대 레벨(20) 에서 XP 추가해도 레벨 고정`() = runTest {
        val character = baseCharacter(level = 20, totalXp = XpThresholds.cumulativeForLevel(20))
        val result = GainXPUseCase(fakeRepo(character)).invoke(99_999)
        assertEquals(20, result?.level)
    }

    @Test
    fun `proficiencyBonus 가 새 레벨에 맞게 갱신`() = runTest {
        // lv1→lv5 (6500 XP 필요)
        val character = baseCharacter()
        val result = GainXPUseCase(fakeRepo(character)).invoke(6_500)
        assertEquals(ProficiencyBonus.forLevel(result!!.level), result.proficiencyBonus)
    }

    @Test
    fun `maxHp 가 새 레벨에 맞게 갱신`() = runTest {
        val character = baseCharacter()
        val result = GainXPUseCase(fakeRepo(character)).invoke(300)
        val expected = HpCalculator.maxHp(CharacterClass.FIGHTER, 2, 10)
        assertEquals(expected, result?.maxHp)
    }
}
