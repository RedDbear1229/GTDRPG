package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HpCalculatorTest {

    @Test
    fun `Lv1 BARBARIAN CON14 최대값 = hitDie + conMod`() {
        // hitDie=12, conMod=+2 → 14
        val hp = HpCalculator.maxHp(CharacterClass.BARBARIAN, level = 1, constitutionScore = 14)
        assertEquals(14, hp)
    }

    @Test
    fun `Lv1 WIZARD CON10 = hitDie(6) + 0`() {
        val hp = HpCalculator.maxHp(CharacterClass.WIZARD, level = 1, constitutionScore = 10)
        assertEquals(6, hp)
    }

    @Test
    fun `Lv5 FIGHTER CON16 = base + 4 평균값`() {
        // hitDie=10, conMod=+3, base=13, perLevel=(10/2+1+3)=9, lv5 → 13 + 4*9 = 49
        val hp = HpCalculator.maxHp(CharacterClass.FIGHTER, level = 5, constitutionScore = 16)
        assertEquals(49, hp)
    }

    @Test
    fun `극단적으로 낮은 CON 에서도 HP 는 1 이상 보장`() {
        val hp = HpCalculator.maxHp(CharacterClass.WIZARD, level = 1, constitutionScore = 1)
        assertTrue(hp >= 1, "HP 가 1 미만으로 떨어지면 안 됨 (actual=$hp)")
    }

    @Test
    fun `level 0 이하는 require 로 거부된다`() {
        assertThrows<IllegalArgumentException> {
            HpCalculator.maxHp(CharacterClass.WIZARD, level = 0, constitutionScore = 10)
        }
    }
}
