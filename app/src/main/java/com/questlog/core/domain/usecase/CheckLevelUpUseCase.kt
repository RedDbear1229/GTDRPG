package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character

// XP 획득 전후 레벨 변화를 감지. true 이면 LevelUpScreen 을 표시해야 한다.
// 중복 발화 방지: 호출자(ViewModel)가 consumed 플래그로 1회만 처리.
object CheckLevelUpUseCase {
    fun didLevelUp(before: Character, after: Character): Boolean =
        after.level > before.level
}
