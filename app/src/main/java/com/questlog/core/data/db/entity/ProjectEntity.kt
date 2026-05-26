package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.ProjectStatus
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
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
