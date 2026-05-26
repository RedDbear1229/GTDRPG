package com.questlog.core.domain.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProficiencyBonusTest {

    @ParameterizedTest
    @CsvSource(
        "1, 2", "2, 2", "3, 2", "4, 2",
        "5, 3", "6, 3", "7, 3", "8, 3",
        "9, 4", "10, 4", "11, 4", "12, 4",
        "13, 5", "16, 5",
        "17, 6", "20, 6",
    )
    fun `Lv1-4 +2, 이후 4 레벨마다 +1 증가`(level: Int, expected: Int) {
        assertEquals(expected, ProficiencyBonus.forLevel(level))
    }

    @Test
    fun `level 0 이하는 require 로 거부된다`() {
        assertThrows<IllegalArgumentException> { ProficiencyBonus.forLevel(0) }
        assertThrows<IllegalArgumentException> { ProficiencyBonus.forLevel(-1) }
    }
}
