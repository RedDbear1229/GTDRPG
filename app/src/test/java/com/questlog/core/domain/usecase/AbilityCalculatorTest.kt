package com.questlog.core.domain.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AbilityCalculatorTest {

    // D&D 5e 능력치 수정치 표. 음수에서 floor 동작 검증이 핵심 — Kotlin 정수 나눗셈은 0 절단이라
    // (9-10)/2 = 0 으로 잘못 계산되므로 Math.floorDiv 가 적용됐는지 확인한다.
    @ParameterizedTest
    @CsvSource(
        "1, -5",
        "2, -4",
        "3, -4",
        "4, -3",
        "9, -1",
        "10, 0",
        "11, 0",
        "12, 1",
        "13, 1",
        "14, 2",
        "16, 3",
        "18, 4",
        "20, 5",
    )
    fun `수정치는 floor((score-10)2) 공식을 따른다`(score: Int, expected: Int) {
        assertEquals(expected, AbilityCalculator.modifier(score))
    }

    @Test
    fun `0 점도 수정치 -5 로 계산된다`() {
        assertEquals(-5, AbilityCalculator.modifier(0))
    }
}
