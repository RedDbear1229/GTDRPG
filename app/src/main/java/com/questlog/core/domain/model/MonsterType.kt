package com.questlog.core.domain.model

// CR ↔ 몬스터 매핑 (docs/04_game_mechanics.md §4.6). CR 자동 매핑 로직은 F3.1 전투 도메인에서 추가.
enum class MonsterType(val defaultCr: Float, val baseXp: Long) {
    DUST_SLIME(0f, 10),
    WEAK_GOBLIN(0.25f, 50),
    GOBLIN(0.5f, 100),
    ORC(1f, 200),
    LIZARDMAN(2f, 450),
    BUGBEAR(3f, 700),
    OGRE(4f, 1_100),
    TROLL(5f, 1_800),
    MANTICORE(6f, 2_300),
    VAMPIRE_SPAWN(7f, 2_900),
    YOUNG_DRAGON(8f, 3_900),
    CLOUD_GIANT(9f, 5_000),
    STONE_GIANT(10f, 5_900),
    ADULT_DRAGON(11f, 7_200),
    ABOLETH(12f, 8_400),
    ANCIENT_DRAGON(15f, 13_000),
    LICH(17f, 18_000),
    PIT_LICH(20f, 25_000),
    TARRASQUE(24f, 62_000),
}
