package com.sa.aichat.providers.anthropic

import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.provider.ChatRequest
import com.sa.aichatlib.provider.ChatResult
import com.sa.aichatlib.provider.ChatRole
import com.sa.aichatlib.provider.ProviderId
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnthropicEngineTest {
	@Test
	fun completeSendsSystemPromptOnlyInTopLevelSystemField() = runBlocking {
		val server = MockWebServer()
		server.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setBody("""{"content":[{"type":"text","text":"Done"}]}""")
		)
		server.start()

		try {
			val engine = AnthropicEngine(
				apiKeyProvider = { "test-key" },
				httpClient = OkHttpClient(),
				endpointUrl = server.url("/v1/messages").toString()
			)

			val result = engine.complete(
				ChatRequest(
					providerId = ProviderId.ANTHROPIC,
					messages = listOf(
						ChatMessagePayload(ChatRole.SYSTEM, "You are concise."),
						ChatMessagePayload(ChatRole.USER, "Summarize this."),
						ChatMessagePayload(ChatRole.ASSISTANT, "Send the text."),
						ChatMessagePayload(ChatRole.USER, "Android SDK")
					)
				)
			)

			assertEquals(ChatResult.Success("Done"), result)

			val recordedRequest = server.takeRequest()
			assertEquals("test-key", recordedRequest.getHeader("x-api-key"))
			assertEquals("2023-06-01", recordedRequest.getHeader("anthropic-version"))

			val body = JSONObject(recordedRequest.body.readUtf8())
			assertEquals("You are concise.", body.getString("system"))

			val messages = body.getJSONArray("messages")
			assertEquals(3, messages.length())
			assertEquals("user", messages.getJSONObject(0).getString("role"))
			assertEquals("Summarize this.", messages.getJSONObject(0).getString("content"))
			assertEquals("assistant", messages.getJSONObject(1).getString("role"))
			assertEquals("Send the text.", messages.getJSONObject(1).getString("content"))
			assertEquals("user", messages.getJSONObject(2).getString("role"))
			assertEquals("Android SDK", messages.getJSONObject(2).getString("content"))
		} finally {
			server.shutdown()
		}
	}

	@Test
	fun completeReturnsErrorWhenApiKeyIsMissing() = runBlocking {
		val engine = AnthropicEngine(apiKeyProvider = { null })

		val result = engine.complete(
			ChatRequest(
				providerId = ProviderId.ANTHROPIC,
				messages = listOf(ChatMessagePayload(ChatRole.USER, "Hello"))
			)
		)

		assertTrue(result is ChatResult.Error)
		assertEquals("Missing Anthropic API key", (result as ChatResult.Error).throwable.message)
	}
}
