package com.questlog.core.data.remote

import com.questlog.core.data.remote.dto.ClaudeMessageRequest
import com.questlog.core.data.remote.dto.ClaudeMessageResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun generateMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeMessageRequest,
    ): ClaudeMessageResponse
}
