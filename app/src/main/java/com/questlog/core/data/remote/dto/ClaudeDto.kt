package com.questlog.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaudeMessageRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String? = null,
    val messages: List<ClaudeMessage>,
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ClaudeMessageResponse(
    val id: String,
    val content: List<ContentBlock>,
    val model: String,
    val usage: Usage? = null,
)

@Serializable
data class ContentBlock(
    val type: String,
    val text: String? = null,
)

@Serializable
data class Usage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
)
