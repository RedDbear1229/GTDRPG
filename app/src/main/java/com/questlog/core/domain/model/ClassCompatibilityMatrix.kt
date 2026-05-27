package com.questlog.core.domain.model

object ClassCompatibilityMatrix {

    private val synergies: Set<Pair<CharacterClass, CharacterClass>> = setOf(
        CharacterClass.FIGHTER to CharacterClass.PALADIN,
        CharacterClass.FIGHTER to CharacterClass.BARBARIAN,
        CharacterClass.PALADIN to CharacterClass.CLERIC,
        CharacterClass.CLERIC to CharacterClass.DRUID,
        CharacterClass.WIZARD to CharacterClass.SORCERER,
        CharacterClass.WIZARD to CharacterClass.WARLOCK,
        CharacterClass.BARD to CharacterClass.WARLOCK,
        CharacterClass.BARD to CharacterClass.SORCERER,
        CharacterClass.ROGUE to CharacterClass.RANGER,
        CharacterClass.MONK to CharacterClass.RANGER,
        CharacterClass.DRUID to CharacterClass.RANGER,
        CharacterClass.MONK to CharacterClass.CLERIC,
    )

    private val tensions: Set<Pair<CharacterClass, CharacterClass>> = setOf(
        CharacterClass.BARBARIAN to CharacterClass.WIZARD,
        CharacterClass.BARBARIAN to CharacterClass.MONK,
        CharacterClass.CLERIC to CharacterClass.WARLOCK,
        CharacterClass.PALADIN to CharacterClass.WARLOCK,
        CharacterClass.PALADIN to CharacterClass.ROGUE,
        CharacterClass.FIGHTER to CharacterClass.SORCERER,
    )

    fun get(a: CharacterClass, b: CharacterClass): CompatibilityLevel {
        if (a == b) return CompatibilityLevel.SYNERGY
        return when {
            synergies.contains(a to b) || synergies.contains(b to a) -> CompatibilityLevel.SYNERGY
            tensions.contains(a to b) || tensions.contains(b to a) -> CompatibilityLevel.TENSION
            else -> CompatibilityLevel.NEUTRAL
        }
    }
}
