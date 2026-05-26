package com.questlog.core.domain.usecase

// D&D 5e 능력치 수정치: floor((score - 10) / 2).
// Kotlin 정수 나눗셈은 0 방향 절단이므로 음수에서 결과가 어긋난다.
// 예: (9 - 10) / 2 = 0 (잘못), 기대값 -1. Math.floorDiv 로 명시 정정.
object AbilityCalculator {
    fun modifier(score: Int): Int = Math.floorDiv(score - 10, 2)
}
