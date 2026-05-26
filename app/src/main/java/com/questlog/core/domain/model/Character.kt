package com.questlog.core.domain.model

import java.util.UUID

// docs/05_data_model.md §5.2 SSOT. 파생 스탯 (proficiencyBonus, armorClass) 은 저장하되
// 계산 진입점은 도메인 usecase (ProficiencyBonus, AbilityCalculator) 가 단일 소스.
data class Character(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val classType: CharacterClass,
    val avatarResId: Int = 0,
    val backstory: String? = null,
    val level: Int = 1,
    val currentXp: Long = 0,
    val totalXpEarned: Long = 0,
    val maxHp: Int,
    val currentHp: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val proficiencyBonus: Int,
    val armorClass: Int = 10,
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long? = null,
    val streakProtectTokens: Int = 0,
    val totalQuestsCompleted: Int = 0,
    val totalMonstersSlain: Int = 0,
    val totalCriticalHits: Int = 0,
    val totalCriticalMisses: Int = 0,
    val totalXpFromCriticals: Long = 0,
    val classResourceCurrent: Int = 0,
    val classResourceMax: Int = 0,
    val classResourceLastRefresh: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
