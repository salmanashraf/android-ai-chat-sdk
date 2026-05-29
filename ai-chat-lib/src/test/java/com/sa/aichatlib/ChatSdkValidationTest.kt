package com.sa.aichatlib

import com.sa.aichatlib.provider.ProviderCredential
import com.sa.aichatlib.provider.ProviderId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSdkValidationTest {
	@Test
	fun validateDefaultProviderReturnsMissingApiKeyError() {
		val result = ChatSdkConfig(defaultProvider = ProviderId.OPEN_AI).validateDefaultProvider()

		assertFalse(result.isValid)
		assertEquals(ProviderId.OPEN_AI, result.provider)
		assertEquals(DEFAULT_OPENAI_MODEL, result.model)
		assertEquals(listOf("OpenAI API key is required."), result.errors)
	}

	@Test
	fun validateDefaultProviderAcceptsConfiguredApiKeyProvider() {
		val result = ChatSdkConfig(
			defaultProvider = ProviderId.XAI,
			credentials = mapOf(ProviderId.XAI to ProviderCredential.ApiKey("xai-key")),
			providerModels = mapOf(ProviderId.XAI to "grok-4.3")
		).validateDefaultProvider()

		assertTrue(result.isValid)
		assertEquals(emptyList<String>(), result.errors)
	}

	@Test
	fun validateDefaultProviderRejectsGeminiJsonWithoutApiKey() {
		val result = ChatSdkConfig(
			defaultProvider = ProviderId.GEMINI,
			credentials = mapOf(ProviderId.GEMINI to ProviderCredential.GoogleServiceJson("""{"project_id":"demo"}"""))
		).validateDefaultProvider()

		assertFalse(result.isValid)
		assertEquals(listOf("Gemini google.json must include api_key or apiKey."), result.errors)
	}

	@Test
	fun requireValidDefaultProviderReturnsConfigWhenValid() {
		val config = ChatSdkConfig(
			defaultProvider = ProviderId.ANTHROPIC,
			credentials = mapOf(ProviderId.ANTHROPIC to ProviderCredential.ApiKey("anthropic-key"))
		)

		assertSame(config, config.requireValidDefaultProvider())
	}
}
