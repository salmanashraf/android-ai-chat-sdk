package com.sa.aichat.providers.xai

import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.provider.ChatRequest
import com.sa.aichatlib.provider.ChatResult
import com.sa.aichatlib.provider.ChatRole
import com.sa.aichatlib.provider.LLMEngine
import com.sa.aichatlib.provider.ProviderId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GrokEngine(
	private val apiKeyProvider: () -> String?,
	private val model: String = "grok-beta",
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val json: Json = Json { ignoreUnknownKeys = true }
) : LLMEngine {

	override val providerId: ProviderId = ProviderId.XAI

	override suspend fun complete(request: ChatRequest): ChatResult {
		val apiKey = apiKeyProvider() ?: return ChatResult.Error(IllegalStateException("Missing XAI API key"))
		val payload = GrokRequest(
			model = request.model ?: model,
			messages = request.messages.map { it.toGrokMessage() }
		)
		val body = json.encodeToString(GrokRequest.serializer(), payload)
			.toRequestBody("application/json".toMediaType())

		return try {
			val httpRequest = Request.Builder()
				.url("https://api.x.ai/v1/chat/completions")
				.header("Authorization", "Bearer $apiKey")
				.post(body)
				.build()

			httpClient.newCall(httpRequest).execute().use { response ->
				if (!response.isSuccessful) {
					return ChatResult.Error(IllegalStateException("Grok error ${response.code}"))
				}
				val responseBody = response.body?.string().orEmpty()
				val parsed = json.decodeFromString(GrokResponse.serializer(), responseBody)
				val content = parsed.choices.firstOrNull()?.message?.content?.trim()
				if (content != null) ChatResult.Success(content) else ChatResult.Error(IllegalStateException("Empty Grok response"))
			}
		} catch (t: Throwable) {
			ChatResult.Error(t)
		}
	}

	private fun ChatMessagePayload.toGrokMessage(): GrokMessage {
		val roleString = when (role) {
			ChatRole.USER -> "user"
			ChatRole.ASSISTANT -> "assistant"
			ChatRole.SYSTEM -> "system"
		}
		return GrokMessage(role = roleString, content = content)
	}
}

@Serializable
private data class GrokRequest(
	val model: String,
	val messages: List<GrokMessage>
)

@Serializable
private data class GrokMessage(
	val role: String,
	val content: String
)

@Serializable
private data class GrokResponse(
	val choices: List<GrokChoice>
)

@Serializable
private data class GrokChoice(
	val message: GrokMessage
)
