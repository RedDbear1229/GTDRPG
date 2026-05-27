package com.questlog.core.domain.model

import java.util.UUID

data class Npc(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val classType: CharacterClass,
    val source: NpcSource,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
