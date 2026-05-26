package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.questlog.core.domain.model.CharacterClass
import java.util.UUID

// 싱글 유저 가정 — 활성 캐릭터는 0/1 개.
// 향후 "secondary characters" 확장 대비 별도 active 플래그 없이 행 수 자체로 표현.
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
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
