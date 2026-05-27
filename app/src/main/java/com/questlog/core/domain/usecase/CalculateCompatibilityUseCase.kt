package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassCompatibilityMatrix
import com.questlog.core.domain.model.CompatibilityLevel
import javax.inject.Inject

class CalculateCompatibilityUseCase @Inject constructor() {
    operator fun invoke(myClass: CharacterClass, npcClass: CharacterClass): CompatibilityLevel =
        ClassCompatibilityMatrix.get(myClass, npcClass)
}
