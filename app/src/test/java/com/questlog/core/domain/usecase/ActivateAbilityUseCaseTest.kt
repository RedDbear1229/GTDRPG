package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassAbilityDef
import com.questlog.core.domain.repository.BuffRepository
import com.questlog.core.domain.repository.CharacterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class ActivateAbilityUseCaseTest {

    private val characterRepository = mockk<CharacterRepository>(relaxUnitFun = true)
    private val buffRepository = mockk<BuffRepository>(relaxUnitFun = true)
    private val useCase = ActivateAbilityUseCase(characterRepository, buffRepository)

    private fun character(cls: CharacterClass, resourceCurrent: Int, resourceMax: Int = 2) = Character(
        name = "Hero", classType = cls, level = 1,
        maxHp = 10, currentHp = 5,
        strength = 10, dexterity = 10, constitution = 10,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
        classResourceCurrent = resourceCurrent,
        classResourceMax = resourceMax,
    )

    @Test
    fun `리소스 없을 때 NoResource 반환`() = runTest {
        coEvery { characterRepository.getActive() } returns character(CharacterClass.FIGHTER, 0)
        assertInstanceOf(ActivateAbilityUseCase.Result.NoResource::class.java, useCase())
    }

    @Test
    fun `캐릭터 없을 때 NoCharacter 반환`() = runTest {
        coEvery { characterRepository.getActive() } returns null
        assertInstanceOf(ActivateAbilityUseCase.Result.NoCharacter::class.java, useCase())
    }

    @Test
    fun `FIGHTER 행동 서지 발동 성공 — 리소스 감소 + 버프 저장`() = runTest {
        val char = character(CharacterClass.FIGHTER, 1, 1)
        coEvery { characterRepository.getActive() } returns char

        val result = useCase()

        assertInstanceOf(ActivateAbilityUseCase.Result.Success::class.java, result)
        val savedChar = slot<Character>()
        coVerify { characterRepository.upsert(capture(savedChar)) }
        assertEquals(0, savedChar.captured.classResourceCurrent)

        val ability = ClassAbilityDef.forClass(CharacterClass.FIGHTER)
        coVerify { buffRepository.setActiveBuff(ability.buffCode) }
    }

    @Test
    fun `CLERIC 치유의 말씀 즉시 HP 회복 — 버프 저장 안 함`() = runTest {
        val char = character(CharacterClass.CLERIC, 1, 1).copy(currentHp = 4, maxHp = 8)
        coEvery { characterRepository.getActive() } returns char

        val result = useCase()

        assertInstanceOf(ActivateAbilityUseCase.Result.Success::class.java, result)
        val savedChar = slot<Character>()
        coVerify { characterRepository.upsert(capture(savedChar)) }
        assert(savedChar.captured.currentHp > char.currentHp) { "HP가 증가해야 함" }
        coVerify(exactly = 0) { buffRepository.setActiveBuff(any()) }
    }

    @Test
    fun `WIZARD 마법 회복 즉시 HP 완전 회복`() = runTest {
        val char = character(CharacterClass.WIZARD, 1, 1).copy(currentHp = 1, maxHp = 6)
        coEvery { characterRepository.getActive() } returns char

        useCase()

        val savedChar = slot<Character>()
        coVerify { characterRepository.upsert(capture(savedChar)) }
        assertEquals(6, savedChar.captured.currentHp, "HP가 maxHp(6)으로 완전 회복되어야 함")
    }

    @Test
    fun `발동 후 리소스가 cost만큼 감소`() = runTest {
        val char = character(CharacterClass.BARBARIAN, 2, 2)
        coEvery { characterRepository.getActive() } returns char

        useCase()

        val savedChar = slot<Character>()
        coVerify { characterRepository.upsert(capture(savedChar)) }
        assertEquals(1, savedChar.captured.classResourceCurrent)
    }
}
