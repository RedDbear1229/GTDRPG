package com.questlog.core.domain.model

// D&D 5e 12 클래스. Hit Die 와 Primary Ability 는 docs/04_game_mechanics.md §4.4 SSOT.
enum class CharacterClass(
    val label: String,
    val hitDie: Int,
    val primaryAbility: AbilityType,
) {
    BARBARIAN(label = "바바리안", hitDie = 12, primaryAbility = AbilityType.STR),
    FIGHTER(label = "파이터", hitDie = 10, primaryAbility = AbilityType.STR),
    PALADIN(label = "팔라딘", hitDie = 10, primaryAbility = AbilityType.STR),
    RANGER(label = "레인저", hitDie = 10, primaryAbility = AbilityType.DEX),
    BARD(label = "바드", hitDie = 8, primaryAbility = AbilityType.CHA),
    CLERIC(label = "클레릭", hitDie = 8, primaryAbility = AbilityType.WIS),
    DRUID(label = "드루이드", hitDie = 8, primaryAbility = AbilityType.WIS),
    MONK(label = "몽크", hitDie = 8, primaryAbility = AbilityType.DEX),
    ROGUE(label = "로그", hitDie = 8, primaryAbility = AbilityType.DEX),
    WARLOCK(label = "워록", hitDie = 8, primaryAbility = AbilityType.CHA),
    SORCERER(label = "소서러", hitDie = 6, primaryAbility = AbilityType.CHA),
    WIZARD(label = "위저드", hitDie = 6, primaryAbility = AbilityType.INT),
}
