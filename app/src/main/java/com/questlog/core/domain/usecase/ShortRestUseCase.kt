package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import javax.inject.Inject
import javax.inject.Singleton

// docs/04_game_mechanics.md §4.2 Short Rest: 연속 3회 퀘스트 완료 시 maxHp × 25% 회복.
// 호출 시점은 CombatViewModel 이 연속 완료 수를 카운팅해 3의 배수일 때 호출.
@Singleton
class ShortRestUseCase @Inject constructor() {
    operator fun invoke(character: Character): Character {
        val heal = (character.maxHp * 0.25f).toInt().coerceAtLeast(1)
        return character.copy(
            currentHp = (character.currentHp + heal).coerceAtMost(character.maxHp),
            updatedAt = System.currentTimeMillis(),
        )
    }
}
