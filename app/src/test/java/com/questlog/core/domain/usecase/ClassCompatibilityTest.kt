package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassCompatibilityMatrix
import com.questlog.core.domain.model.CompatibilityLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClassCompatibilityTest {

    @Test
    fun `같은 클래스끼리는 항상 SYNERGY`() {
        CharacterClass.entries.forEach { cls ->
            assertEquals(CompatibilityLevel.SYNERGY, ClassCompatibilityMatrix.get(cls, cls),
                "$cls vs $cls 는 SYNERGY 여야 함")
        }
    }

    @Test
    fun `호환성은 순서 무관 대칭 반환`() {
        CharacterClass.entries.forEach { a ->
            CharacterClass.entries.forEach { b ->
                assertEquals(
                    ClassCompatibilityMatrix.get(a, b),
                    ClassCompatibilityMatrix.get(b, a),
                    "$a vs $b 와 $b vs $a 결과가 달라서는 안 됨",
                )
            }
        }
    }

    @Test
    fun `FIGHTER-PALADIN 시너지 확인`() {
        assertEquals(CompatibilityLevel.SYNERGY,
            ClassCompatibilityMatrix.get(CharacterClass.FIGHTER, CharacterClass.PALADIN))
    }

    @Test
    fun `BARBARIAN-WIZARD 긴장 확인`() {
        assertEquals(CompatibilityLevel.TENSION,
            ClassCompatibilityMatrix.get(CharacterClass.BARBARIAN, CharacterClass.WIZARD))
    }

    @Test
    fun `PALADIN-WARLOCK 긴장 확인`() {
        assertEquals(CompatibilityLevel.TENSION,
            ClassCompatibilityMatrix.get(CharacterClass.PALADIN, CharacterClass.WARLOCK))
    }

    @Test
    fun `WIZARD-SORCERER 시너지 확인`() {
        assertEquals(CompatibilityLevel.SYNERGY,
            ClassCompatibilityMatrix.get(CharacterClass.WIZARD, CharacterClass.SORCERER))
    }

    @Test
    fun `CalculateCompatibilityUseCase가 Matrix 위임`() {
        val useCase = CalculateCompatibilityUseCase()
        val result = useCase(CharacterClass.FIGHTER, CharacterClass.PALADIN)
        assertEquals(CompatibilityLevel.SYNERGY, result)
    }
}
