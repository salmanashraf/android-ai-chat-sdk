package com.sa.aichat.providers.anthropic

import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.provider.ChatRequest
import com.sa.aichatlib.provider.ChatResult
import com.sa.aichatlib.provider.ChatRole
import com.sa.aichatlib.provider.LLMEngine
import com.sa.aichatlib.provider.ProviderId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AnthropicEngine(
	private val apiKeyProvider: () -> String?,
	private val model: String = "claude-3-haiku-20240307",
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val json: Json = Json { ignoreUnknownKeys = true }
) : LLMEngine {

	override val providerId: ProviderId = ProviderId.ANTHROPIC

	override suspend fun complete(request: ChatRequest): ChatResult {
		val apiKey = apiKeyProvider() ?: return ChatResult.Error(IllegalStateException("Missing Anthropic API key"))
		val payload = AnthropicRequest(
			model = request.model ?: model,
			messages = request.messages.map { it.toAnthropic() },
			system = request.messages.firstOrNull { it.role == ChatRole.SYSTEM }?.content
		)
		val body = json.encodeToString(AnthropicRequest.serializer(), payload)
			.toRequestBody("application/json".toMediaType())

		return try {
			val httpRequest = Request.Builder()
				.url("https://api.anthropic.com/v1/messages")
				.header("x-api-key", apiKey)
				.header("anthropic-version", "2023-06-01")
				.post(body)
				.build()

			httpClient.newCall(httpRequest).execute().use { response ->
				if (!response.isSuccessful) {
					return ChatResult.Error(IllegalStateException("Anthropic error ${response.code}"))
				}
				val responseBody = response.body?.string().orEmpty()
				val parsed = json.decodeFromString(AnthropicResponse.serializer(), responseBody)
				val content = parsed.content.firstOrNull()?.text?.trim()
				if (content != null) ChatResult.Success(content) else ChatResult.Error(IllegalStateException("Empty Anthropic response"))
			}
		} catch (t: Throwable) {
			ChatResult.Error(t)
		}
	}

	private fun ChatMessagePayload.toAnthropic(): AnthropicMessage {
		val roleString = when (role) {
			ChatRole.USER -> "user"
			ChatRole.ASSISTANT -> "assistant"
			ChatRole.SYSTEM -> "user"
		}
		return AnthropicMessage(role = roleString, content = content)
	}
}

@Serializable
private data class AnthropicRequest(
	val model: String,
	val messages: List<AnthropicMessage>,
	val system: String? = null
)

@Serializable
private data class AnthropicMessage(
	val role: String,
	val content: String
)

@Serializable
private data class AnthropicResponse(
	val content: List<AnthropicContent> = emptyList()
)

@Serializable
private data class AnthropicContent(
	@SerialName("text") val text: String,
	@SerialName("type") val type: String
)
