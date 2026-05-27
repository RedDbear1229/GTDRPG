package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.ItemCatalog
import com.questlog.core.domain.model.ItemRarity
import com.questlog.core.domain.model.ItemTemplate
import kotlin.random.Random

// 04_game_mechanics.md §4.5 드롭률 SSOT.
// 순수 계산 (사이드이펙트 없음) — 실제 DB 저장은 호출자(ViewModel) 책임.
class ItemDropUseCase(
    private val random: Random = Random.Default,
) {
    operator fun invoke(cr: Float, isCriticalHit: Boolean): ItemTemplate? {
        val rarity = pickRarity(cr, isCriticalHit) ?: return null
        val candidates = ItemCatalog.getDropCandidates(rarity)
        return if (candidates.isEmpty()) null else candidates.random(random)
    }

    // 등급 결정: 드롭 조건(CR 제약) + 확률 롤
    private fun pickRarity(cr: Float, isCriticalHit: Boolean): ItemRarity? {
        // Legendary: 크리티컬 히트만
        if (isCriticalHit && random.nextFloat() < ItemRarity.LEGENDARY.dropChance) {
            return ItemRarity.LEGENDARY
        }
        // Very Rare: CR 12+ 명중 또는 크리티컬
        if ((cr >= 12f || isCriticalHit) && random.nextFloat() < ItemRarity.VERY_RARE.dropChance) {
            return ItemRarity.VERY_RARE
        }
        // Rare: CR 7+ 명중
        if (cr >= 7f && random.nextFloat() < ItemRarity.RARE.dropChance) {
            return ItemRarity.RARE
        }
        // Uncommon: CR 3+ 명중
        if (cr >= 3f && random.nextFloat() < ItemRarity.UNCOMMON.dropChance) {
            return ItemRarity.UNCOMMON
        }
        // Common: 모든 명중
        if (random.nextFloat() < ItemRarity.COMMON.dropChance) {
            return ItemRarity.COMMON
        }
        return null
    }
}
