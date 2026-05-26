package com.questlog.core.domain.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class XpThresholdsTest {

    @Test
    fun `Lv1 누적 XP = 0`() {
        assertEquals(0L, XpThresholds.cumulativeForLevel(1))
    }

    @Test
    fun `Lv20 누적 XP = 355000`() {
        assertEquals(355_000L, XpThresholds.cumulativeForLevel(20))
    }

    @Test
    fun `XP 0 은 Lv1`() {
        assertEquals(1, XpThresholds.levelForXp(0L))
    }

    @Test
    fun `XP 299 는 아직 Lv1`() {
        assertEquals(1, XpThresholds.levelForXp(299L))
    }

    @Test
    fun `XP 300 도달 시 Lv2`() {
        assertEquals(2, XpThresholds.levelForXp(300L))
    }

    @Test
    fun `XP 355000 도달 시 Lv20 cap`() {
        assertEquals(20, XpThresholds.levelForXp(355_000L))
    }

    @Test
    fun `Lv20 초과 XP 도 여전히 Lv20`() {
        assertEquals(20, XpThresholds.levelForXp(1_000_000L))
    }

    @Test
    fun `Lv1 에서 다음 레벨까지 = 300 XP`() {
        assertEquals(300L, XpThresholds.xpToNextLevel(0L))
    }

    @Test
    fun `Lv20 도달 시 xpToNextLevel 은 null`() {
        assertNull(XpThresholds.xpToNextLevel(355_000L))
        assertNull(XpThresholds.xpToNextLevel(500_000L))
    }

    @Test
    fun `잘못된 level 입력은 require 로 거부된다`() {
        assertThrows<IllegalArgumentException> { XpThresholds.cumulativeForLevel(0) }
        assertThrows<IllegalArgumentException> { XpThresholds.cumulativeForLevel(21) }
        assertThrows<IllegalArgumentException> { XpThresholds.levelForXp(-1L) }
    }
}
