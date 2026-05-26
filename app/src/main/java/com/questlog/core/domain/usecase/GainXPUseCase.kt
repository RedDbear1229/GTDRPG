package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.repository.CharacterRepository
import javax.inject.Inject

class GainXPUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(xpAmount: Long): Character? {
        val character = characterRepository.getActive() ?: return null
        val newTotalXp = character.totalXpEarned + xpAmount
        val newLevel = XpThresholds.levelForXp(newTotalXp).coerceAtMost(XpThresholds.MAX_LEVEL)
        val newCurrentXp = newTotalXp - XpThresholds.cumulativeForLevel(newLevel)
        val newProfBonus = ProficiencyBonus.forLevel(newLevel)
        val newMaxHp = HpCalculator.maxHp(character.classType, newLevel, character.constitution)
        val updated = character.copy(
            level = newLevel,
            currentXp = newCurrentXp.coerceAtLeast(0),
            totalXpEarned = newTotalXp,
            proficiencyBonus = newProfBonus,
            maxHp = newMaxHp,
            // HP는 레벨업 시 최대값으로 회복하지 않음 — Long Rest(자정) 담당
            updatedAt = System.currentTimeMillis(),
        )
        characterRepository.upsert(updated)
        return updated
    }
}
