package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.LifeArea

// docs/03_gtd_system.md §3.3 — CR 자동 계산. 가중치(0.4/0.35/0.25)는 추정값, 실사용 데이터로 보정 예정.
object CrCalculator {

    fun calculate(
        estimatedMinutes: Int,
        hasDeadline: Boolean,
        daysUntilDeadline: Int?,
        complexitySteps: Int,
        @Suppress("UNUSED_PARAMETER") lifeArea: LifeArea,
        isRecurring: Boolean,
    ): Float {
        val timeScore = when {
            estimatedMinutes <= 2 -> 0f
            estimatedMinutes <= 15 -> 0.25f
            estimatedMinutes <= 30 -> 0.5f
            estimatedMinutes <= 60 -> 1f
            estimatedMinutes <= 120 -> 2f
            estimatedMinutes <= 240 -> 3f
            estimatedMinutes <= 480 -> 5f
            estimatedMinutes <= 960 -> 8f
            else -> 11f
        }
        val urgencyScore = if (hasDeadline && daysUntilDeadline != null) {
            when {
                daysUntilDeadline <= 0 -> 10f
                daysUntilDeadline <= 1 -> 8f
                daysUntilDeadline <= 3 -> 5f
                daysUntilDeadline <= 7 -> 3f
                daysUntilDeadline <= 30 -> 1f
                else -> 0f
            }
        } else 0f
        val complexityScore = (complexitySteps * 1.5f).coerceAtMost(10f)
        val rawCR = timeScore * 0.4f + urgencyScore * 0.35f + complexityScore * 0.25f
        val routineMultiplier = if (isRecurring) 0.7f else 1f
        return (rawCR * routineMultiplier).coerceIn(0f, 30f)
    }
}
