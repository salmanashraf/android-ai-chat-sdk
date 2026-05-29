package com.sa.aichatlib.provider

import kotlinx.serialization.Serializable

data class ChatRequest(
    val providerId: ProviderId,
    val messages: List<ChatMessagePayload>,
    val model: String? = null
)

@Serializable
data class ChatMessagePayload(
    val role: ChatRole,
    val content: String
)

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}

sealed class ChatResult {
    data class Success(val content: String) : ChatResult()
    data class Error(val throwable: Throwable) : ChatResult()
}

interface LLMEngine {
    val providerId: ProviderId
    suspend fun complete(request: ChatRequest): ChatResult
}
