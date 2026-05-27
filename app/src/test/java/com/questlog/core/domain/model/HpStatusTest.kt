package com.questlog.core.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HpStatusTest {

    @Test fun `100% → HEALTHY`() = assertEquals(HpStatus.HEALTHY, HpStatus.of(40, 40))
    @Test fun `76% → HEALTHY`() = assertEquals(HpStatus.HEALTHY, HpStatus.of(31, 40))
    @Test fun `75% → TIRED`() = assertEquals(HpStatus.TIRED, HpStatus.of(30, 40))
    @Test fun `51% → TIRED`() = assertEquals(HpStatus.TIRED, HpStatus.of(21, 40))
    @Test fun `50% → WOUNDED`() = assertEquals(HpStatus.WOUNDED, HpStatus.of(20, 40))
    @Test fun `26% → WOUNDED`() = assertEquals(HpStatus.WOUNDED, HpStatus.of(11, 40))
    @Test fun `25% → CRITICAL`() = assertEquals(HpStatus.CRITICAL, HpStatus.of(10, 40))
    @Test fun `1% → CRITICAL`() = assertEquals(HpStatus.CRITICAL, HpStatus.of(1, 40))
    @Test fun `0 → UNCONSCIOUS`() = assertEquals(HpStatus.UNCONSCIOUS, HpStatus.of(0, 40))
    @Test fun `maxHp 0 → UNCONSCIOUS`() = assertEquals(HpStatus.UNCONSCIOUS, HpStatus.of(0, 0))
}
