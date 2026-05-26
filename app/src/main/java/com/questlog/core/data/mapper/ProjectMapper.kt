package com.questlog.core.data.mapper

import com.questlog.core.data.db.entity.ProjectEntity
import com.questlog.core.domain.model.Project

fun ProjectEntity.toDomain() = Project(
    id = id,
    title = title,
    desiredOutcome = desiredOutcome,
    campaignName = campaignName,
    description = description,
    status = status,
    lifeArea = lifeArea,
    challengeRating = challengeRating,
    totalTaskCount = totalTaskCount,
    completedTaskCount = completedTaskCount,
    dueDate = dueDate,
    startDate = startDate,
    xpReward = xpReward,
    isMilestone = isMilestone,
    createdAt = createdAt,
    completedAt = completedAt,
    updatedAt = updatedAt,
)

fun Project.toEntity() = ProjectEntity(
    id = id,
    title = title,
    desiredOutcome = desiredOutcome,
    campaignName = campaignName,
    description = description,
    status = status,
    lifeArea = lifeArea,
    challengeRating = challengeRating,
    totalTaskCount = totalTaskCount,
    completedTaskCount = completedTaskCount,
    dueDate = dueDate,
    startDate = startDate,
    xpReward = xpReward,
    isMilestone = isMilestone,
    createdAt = createdAt,
    completedAt = completedAt,
    updatedAt = updatedAt,
)
