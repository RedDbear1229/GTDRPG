package com.questlog.core.domain.model

data class ClassAbility(
    val key: String,
    val nameKo: String,
    val nameEn: String,
    val description: String,
    val flavorText: String,
    val resourceName: String,
    val resourceMax: Int,
    val cost: Int,
    val buffEffect: BuffEffectType,
    val buffValue: Int,
    val isImmediate: Boolean,
) {
    // 전송 포맷: "EFFECT_TYPE:VALUE" (DataStore 직렬화)
    val buffCode: String get() = "${buffEffect.name}:$buffValue"
}
