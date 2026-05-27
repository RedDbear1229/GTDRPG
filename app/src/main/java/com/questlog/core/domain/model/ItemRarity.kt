package com.questlog.core.domain.model

// 드롭률: 04_game_mechanics.md §4.5
enum class ItemRarity(val dropChance: Float, val colorHex: String) {
    COMMON     (0.15f, "#9E9E9E"),
    UNCOMMON   (0.08f, "#4CAF50"),
    RARE       (0.04f, "#2196F3"),
    VERY_RARE  (0.02f, "#9C27B0"),
    LEGENDARY  (0.005f, "#C19A6B"),
}
