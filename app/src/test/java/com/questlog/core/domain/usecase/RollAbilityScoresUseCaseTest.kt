package com.questlog.core.domain.usecase

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class RollAbilityScoresUseCaseTest {

    private val fixedRandom = Random(seed = 42)
    private val useCase = RollAbilityScoresUseCase(random = fixedRandom)

    @Test
    fun `rollOne 결과는 3-18 범위 안에 있다`() {
        // 4d6 drop lowest: 최소 3(1+1+1), 최대 18(6+6+6)
        val useCase2 = RollAbilityScoresUseCase(random = Random.Default)
        repeat(1000) {
            val score = useCase2.rollOne()
            assertTrue(score in 3..18, "범위 초과: $score")
        }
    }

    @Test
    fun `rollAll 은 6개 능력치를 모두 반환한다`() {
        val scores = useCase.rollAll()
        assertNotNull(scores.strength)
        assertNotNull(scores.dexterity)
        assertNotNull(scores.constitution)
        assertNotNull(scores.intelligence)
        assertNotNull(scores.wisdom)
        assertNotNull(scores.charisma)
    }

    @Test
    fun `4d6 drop lowest — 합산은 나머지 3주사위 합과 일치해야 한다`() {
        // 시드 고정 Random 으로 rollOne 을 직접 모의: 4개 중 최솟값 1개 제거
        val deterministicRandom = object : Random() {
            private val values = listOf(3, 5, 6, 2) // drop lowest = 2, sum = 14
            private var idx = 0
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextInt(from: Int, until: Int): Int {
                val v = values[idx % values.size]
                idx++
                return v
            }
        }
        val score = RollAbilityScoresUseCase(deterministicRandom).rollOne()
        // 5+6+3 = 14 (2 제거)
        assertTrue(score == 14, "expected 14, got $score")
    }
}
