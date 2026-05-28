package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.ClaimResult
import com.questlog.core.domain.model.EncounterStatus
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.EncounterRepository
import javax.inject.Inject

class ClaimEncounterRewardUseCase @Inject constructor(
    private val encounterRepository: EncounterRepository,
    private val characterRepository: CharacterRepository,
) {
    sealed class Result {
        data class Success(val xpGained: Long, val newLevel: Int) : Result()
        object NotFound : Result()
        object AlreadyClaimed : Result()
        object NoCharacter : Result()
    }

    suspend operator fun invoke(encounterId: String): Result {
        val character = characterRepository.getActive() ?: return Result.NoCharacter
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        if (encounter.isExpired || encounter.status != EncounterStatus.PENDING) return Result.AlreadyClaimed

        val rewardXp = encounter.rewardXp
        val claimResult = encounterRepository.claimReward(encounterId, character.id, rewardXp)
        if (claimResult == ClaimResult.AlreadyClaimedOrExpired) return Result.AlreadyClaimed

        // 캐릭터 XP/레벨 업데이트
        val newTotalXp = character.totalXpEarned + rewardXp
        val newLevel = XpThresholds.levelForXp(newTotalXp).coerceAtMost(XpThresholds.MAX_LEVEL)
        val newCurrentXp = (newTotalXp - XpThresholds.cumulativeForLevel(newLevel)).coerceAtLeast(0)
        val updated = character.copy(
            level = newLevel,
            currentXp = newCurrentXp,
            totalXpEarned = newTotalXp,
            proficiencyBonus = ProficiencyBonus.forLevel(newLevel),
            maxHp = HpCalculator.maxHp(character.classType, newLevel, character.constitution),
            updatedAt = System.currentTimeMillis(),
        )
        characterRepository.upsert(updated)
        return Result.Success(xpGained = rewardXp, newLevel = newLevel)
    }
}
