package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.BuffEffectType
import com.questlog.core.domain.model.ClassAbility
import com.questlog.core.domain.model.ClassAbilityDef
import com.questlog.core.domain.repository.BuffRepository
import com.questlog.core.domain.repository.CharacterRepository
import javax.inject.Inject

class ActivateAbilityUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val buffRepository: BuffRepository,
) {
    sealed class Result {
        object Success : Result()
        object NoResource : Result()
        object NoCharacter : Result()
    }

    suspend operator fun invoke(): Result {
        val character = characterRepository.getActive() ?: return Result.NoCharacter
        val ability = ClassAbilityDef.forClass(character.classType)

        if (character.classResourceCurrent < ability.cost) return Result.NoResource

        val updatedCharacter = if (ability.isImmediate) {
            applyImmediate(character, ability)
        } else {
            character
        }

        characterRepository.upsert(
            updatedCharacter.copy(
                classResourceCurrent = updatedCharacter.classResourceCurrent - ability.cost,
                updatedAt = System.currentTimeMillis(),
            )
        )

        if (!ability.isImmediate) {
            buffRepository.setActiveBuff(ability.buffCode)
        }

        return Result.Success
    }

    private fun applyImmediate(
        character: com.questlog.core.domain.model.Character,
        ability: ClassAbility,
    ) = when (ability.buffEffect) {
        BuffEffectType.HP_RESTORE -> {
            val heal = (character.maxHp * ability.buffValue / 100).coerceAtLeast(1)
            character.copy(currentHp = (character.currentHp + heal).coerceAtMost(character.maxHp))
        }
        else -> character
    }
}
