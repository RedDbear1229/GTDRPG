package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import javax.inject.Inject
import javax.inject.Singleton

// docs/04_game_mechanics.md §4.1 calculateHPLoss.
// HP는 0 미만으로 내려가지 않는다.
@Singleton
class ApplyHpDamageUseCase @Inject constructor() {
    operator fun invoke(character: Character, damage: Int): Character =
        character.copy(
            currentHp = (character.currentHp - damage).coerceAtLeast(0),
            updatedAt = System.currentTimeMillis(),
        )
}
