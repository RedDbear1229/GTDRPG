package com.questlog.core.domain.model

// 색상은 UI 레이어 (core/ui/theme/LifeAreaColors.kt) — 도메인은 순수 Kotlin 유지
enum class LifeArea(
    val label: String,
    val icon: String,
    val primaryAbility: AbilityType,
) {
    WORK("업무", "🏢", AbilityType.INT),
    HEALTH("건강", "💪", AbilityType.CON),
    LEARNING("학습", "📚", AbilityType.INT),
    RELATIONSHIP("관계", "👥", AbilityType.CHA),
    FINANCE("재정", "💰", AbilityType.WIS),
    PERSONAL("개인", "✨", AbilityType.WIS),
    CREATIVE("창작", "🎨", AbilityType.CHA),
}
