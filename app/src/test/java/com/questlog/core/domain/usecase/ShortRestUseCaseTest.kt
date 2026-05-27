package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.Character
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ShortRestUseCaseTest {

    private val useCase = ShortRestUseCase()

    @Test
    fun `HP 25% 회복, maxHp 초과 불가`() {
        val c = character(currentHp = 20, maxHp = 40)
        val result = useCase(c)
        assertEquals(30, result.currentHp) // 20 + 10 (40*0.25)
    }

    @Test
    fun `회복 후 maxHp 초과 시 maxHp 로 캡`() {
        val c = character(currentHp = 38, maxHp = 40)
        val result = useCase(c)
        assertEquals(40, result.currentHp)
    }

    @Test
    fun `최소 회복량은 1`() {
        val c = character(currentHp = 0, maxHp = 3) // 3*0.25=0 → coerce to 1
        val result = useCase(c)
        assertEquals(1, result.currentHp)
    }

    private fun character(currentHp: Int, maxHp: Int) = Character(
        name = "Tester",
        classType = CharacterClass.CLERIC,
        level = 1,
        currentXp = 0,
        totalXpEarned = 0,
        maxHp = maxHp,
        currentHp = currentHp,
        strength = 10, dexterity = 10, constitution = 10,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )
}
