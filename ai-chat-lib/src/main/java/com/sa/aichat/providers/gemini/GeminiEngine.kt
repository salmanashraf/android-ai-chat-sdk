package com.sa.aichat.providers.gemini

import com.sa.aichatlib.DEFAULT_GEMINI_MODEL
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

class GeminiEngine(
	private val apiKeyProvider: () -> String?,
	private val model: String = DEFAULT_GEMINI_MODEL,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val json: Json = Json { ignoreUnknownKeys = true }
) : LLMEngine {

	override val providerId: ProviderId = ProviderId.GEMINI

	override suspend fun complete(request: ChatRequest): ChatResult {
		val apiKey = apiKeyProvider() ?: return ChatResult.Error(IllegalStateException("Missing Gemini API key"))
		val payload = GeminiRequest(
			contents = request.messages.map { msg ->
				Content(parts = listOf(ContentPart(text = msg.content)), role = msg.role.toGeminiRole())
			}
		)
		val body = json.encodeToString(GeminiRequest.serializer(), payload)
			.toRequestBody("application/json".toMediaType())

		return try {
			val httpRequest = Request.Builder()
				.url("https://generativelanguage.googleapis.com/v1beta/${request.model ?: model}:generateContent?key=$apiKey")
				.post(body)
				.build()

			httpClient.newCall(httpRequest).execute().use { response ->
				if (!response.isSuccessful) {
					return ChatResult.Error(IllegalStateException("Gemini error ${response.code}"))
				}
				val responseBody = response.body?.string().orEmpty()
				val parsed = json.decodeFromString(GeminiResponse.serializer(), responseBody)
				val content = parsed.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
				if (content != null) ChatResult.Success(content) else ChatResult.Error(IllegalStateException("Empty Gemini response"))
			}
		} catch (t: Throwable) {
			ChatResult.Error(t)
		}
	}

	private fun ChatRole.toGeminiRole(): String =
		when (this) {
			ChatRole.USER -> "user"
			ChatRole.ASSISTANT -> "model"
			ChatRole.SYSTEM -> "system"
		}
}

@Serializable
private data class GeminiRequest(
	val contents: List<Content>
)

@Serializable
private data class Content(
	val parts: List<ContentPart>,
	val role: String? = null
)

@Serializable
private data class ContentPart(
	val text: String
)

@Serializable
private data class GeminiResponse(
	val candidates: List<Candidate> = emptyList()
)

@Serializable
private data class Candidate(
	val content: Content,
	@SerialName("finishReason") val finishReason: String? = null
)
