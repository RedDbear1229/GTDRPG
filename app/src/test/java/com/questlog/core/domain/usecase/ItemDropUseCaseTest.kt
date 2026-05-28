package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.ItemCatalog
import com.questlog.core.domain.model.ItemRarity
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ItemDropUseCaseTest {

    // nextFloat() 는 내부적으로 nextBits(24) 를 호출하므로, 반환값 고정 불가 → 확정 Random 사용.

    private fun useCaseWithFloat(fixedFloat: Float): ItemDropUseCase {
        val random = object : Random() {
            override fun nextBits(bitCount: Int): Int {
                val target = (fixedFloat * (1 shl 24)).toInt().coerceIn(0, (1 shl 24) - 1)
                // Mask by bitCount so power-of-2 list indices don't go out of bounds.
                return if (bitCount == 0) 0 else target and ((1 shl bitCount) - 1)
            }
        }
        return ItemDropUseCase(random)
    }

    @Test
    fun `크리티컬 히트 + 확률 범위 내 → 전설 아이템 드롭`() {
        val useCase = useCaseWithFloat(0.001f) // < LEGENDARY.dropChance(0.005)
        val result = useCase(cr = 15f, isCriticalHit = true)
        assertNotNull(result)
        assert(result!!.rarity == ItemRarity.LEGENDARY) { "Expected LEGENDARY, got ${result.rarity}" }
    }

    @Test
    fun `크리티컬 미스 아님 + 확률 초과 → null 반환`() {
        val useCase = useCaseWithFloat(0.99f) // > 모든 드롭 확률
        val result = useCase(cr = 20f, isCriticalHit = false)
        assertNull(result)
    }

    @Test
    fun `CR 1 일반 명중 + Common 확률 내 → Common 아이템`() {
        // Common 드롭률 0.15f. 0.10f 이면 LEGENDARY/VERY_RARE/RARE/UNCOMMON 실패, Common 통과
        // 하지만 CR<3 이면 Uncommon 도 차단됨 → Common 만 가능
        val useCase = useCaseWithFloat(0.10f)
        val result = useCase(cr = 1f, isCriticalHit = false)
        assertNotNull(result)
        assert(result!!.rarity == ItemRarity.COMMON)
    }

    @Test
    fun `카탈로그 전 등급 아이템 30개 이상 존재`() {
        assert(ItemCatalog.ALL.size >= 30) { "아이템 카탈로그가 30개 미만: ${ItemCatalog.ALL.size}" }
    }

    @Test
    fun `모든 등급에 최소 1개 아이템 존재`() {
        ItemRarity.entries.forEach { rarity ->
            val count = ItemCatalog.getDropCandidates(rarity).size
            assert(count >= 1) { "등급 $rarity 에 아이템 없음" }
        }
    }

    @Test
    fun `크리티컬 히트 아님이면 Legendary 드롭 불가`() {
        // fixedFloat = 0.001 이면 LEGENDARY 확률 0.005 미만이지만, 크리티컬 아니면 체크 안 함
        val useCase = useCaseWithFloat(0.001f)
        val result = useCase(cr = 20f, isCriticalHit = false)
        // LEGENDARY 체크가 스킵 → VERY_RARE 체크 (0.001 < 0.02) → VERY_RARE 드롭
        if (result != null) {
            assert(result.rarity != ItemRarity.LEGENDARY) { "크리티컬 없이 LEGENDARY 드롭 불가" }
        }
    }
}
