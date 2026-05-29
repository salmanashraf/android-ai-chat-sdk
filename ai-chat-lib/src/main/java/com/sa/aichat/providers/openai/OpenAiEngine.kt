package com.sa.aichat.providers.openai

import com.sa.aichatlib.model.OpenAIChatRequest
import com.sa.aichatlib.model.OpenAIChatResponse
import com.sa.aichatlib.model.OpenAIMessage
import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.provider.ChatRequest
import com.sa.aichatlib.provider.ChatResult
import com.sa.aichatlib.provider.ChatRole
import com.sa.aichatlib.provider.LLMEngine
import com.sa.aichatlib.provider.ProviderId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiEngine(
    private val apiKeyProvider: () -> String?,
    private val defaultModel: String = "gpt-3.5-turbo",
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LLMEngine {

    override val providerId: ProviderId = ProviderId.OPEN_AI

    override suspend fun complete(request: ChatRequest): ChatResult {
        val apiKey = apiKeyProvider()
            ?: return ChatResult.Error(IllegalStateException("Missing OpenAI API key"))

        val payload = OpenAIChatRequest(
            model = request.model ?: defaultModel,
            messages = request.messages.map { it.toOpenAI() }
        )
        val body = json.encodeToString(payload)
            .toRequestBody("application/json".toMediaType())

        return try {
            val httpRequest = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            httpClient.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return ChatResult.Error(IllegalStateException("OpenAI error ${response.code}"))
                }
                val responseBody = response.body?.string().orEmpty()
                val parsed = json.decodeFromString<OpenAIChatResponse>(responseBody)
                val content = parsed.choices.firstOrNull()?.message?.content?.trim()
                if (content != null) {
                    ChatResult.Success(content)
                } else {
                    ChatResult.Error(IllegalStateException("Empty response from OpenAI"))
                }
            }
        } catch (t: Throwable) {
            ChatResult.Error(t)
        }
    }

    private fun ChatMessagePayload.toOpenAI(): OpenAIMessage {
        val roleString = when (role) {
            ChatRole.USER -> "user"
            ChatRole.ASSISTANT -> "assistant"
            ChatRole.SYSTEM -> "system"
        }
        return OpenAIMessage(role = roleString, content = content)
    }
}
