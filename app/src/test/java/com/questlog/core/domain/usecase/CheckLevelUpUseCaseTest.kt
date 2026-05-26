package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CheckLevelUpUseCaseTest {

    private fun makeCharacter(level: Int) = Character(
        name = "Test",
        classType = CharacterClass.FIGHTER,
        level = level,
        maxHp = 10,
        currentHp = 10,
        strength = 10, dexterity = 10, constitution = 10,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = ProficiencyBonus.forLevel(level),
    )

    @Test
    fun `레벨이 증가하면 didLevelUp 은 true`() {
        val before = makeCharacter(1)
        val after = makeCharacter(2)
        assertTrue(CheckLevelUpUseCase.didLevelUp(before, after))
    }

    @Test
    fun `레벨이 동일하면 didLevelUp 은 false`() {
        val character = makeCharacter(3)
        assertFalse(CheckLevelUpUseCase.didLevelUp(character, character.copy()))
    }

    @Test
    fun `레벨이 감소하면 didLevelUp 은 false`() {
        val before = makeCharacter(5)
        val after = makeCharacter(4)
        assertFalse(CheckLevelUpUseCase.didLevelUp(before, after))
    }

    @Test
    fun `최대 레벨(20) 에서 동일하면 false`() {
        val at20 = makeCharacter(20)
        assertFalse(CheckLevelUpUseCase.didLevelUp(at20, at20.copy()))
    }
}
