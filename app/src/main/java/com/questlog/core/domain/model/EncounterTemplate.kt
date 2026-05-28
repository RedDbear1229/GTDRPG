package com.questlog.core.domain.model

data class EncounterTemplate(
    val key: String,
    val title: String,
    val description: String,
    val flavorText: String,
    val minCr: Float,
    val maxCr: Float,
    val baseXp: Long,
)
