package com.questlog.core.domain.model

import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val desiredOutcome: String? = null,
    val campaignName: String? = null,
    val description: String? = null,
    val status: ProjectStatus,
    val lifeArea: LifeArea,
    val challengeRating: Float,
    val totalTaskCount: Int = 0,
    val completedTaskCount: Int = 0,
    val dueDate: Long? = null,
    val startDate: Long? = null,
    val xpReward: Long = 0,
    val isMilestone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)
