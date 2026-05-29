package com.sa.aichatlib

import android.content.Context
import com.sa.aichat.providers.anthropic.AnthropicEngine
import com.sa.aichat.providers.gemini.GeminiEngine
import com.sa.aichat.providers.openai.OpenAiEngine
import com.sa.aichat.providers.xai.GrokEngine
import com.sa.aichatlib.provider.ProviderCredential
import com.sa.aichatlib.provider.ProviderId
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.json.JSONObject

const val DEFAULT_OPENAI_MODEL = "gpt-4.1"
const val DEFAULT_GEMINI_MODEL = "models/gemini-3.5-flash"
const val DEFAULT_ANTHROPIC_MODEL = "claude-sonnet-4-20250514"
const val DEFAULT_XAI_MODEL = "grok-4.3"

/**
 * Convenience helpers to bootstrap ChatSdk with the default provider engines.
 */
fun ChatSdk.initializeWithDefaults(
	context: Context,
	config: ChatSdkConfig = ChatSdkConfig()
) {
	initialize(context, config)
	installDefaultEngines(config = config)
}

fun ChatSdk.applyConfig(
	config: ChatSdkConfig,
	httpClient: OkHttpClient = OkHttpClient(),
	json: Json = Json { ignoreUnknownKeys = true }
) {
	updateConfig(config)
	installDefaultEngines(config = config, httpClient = httpClient, json = json)
}

fun ChatSdk.installDefaultEngines(
	config: ChatSdkConfig = config(),
	httpClient: OkHttpClient = OkHttpClient(),
	json: Json = Json { ignoreUnknownKeys = true }
) {
	val cfg = config
	(cfg.credentials[ProviderId.OPEN_AI] as? ProviderCredential.ApiKey)?.key?.let { openAiKey ->
		registerEngine(
			OpenAiEngine(
				apiKeyProvider = { openAiKey },
				defaultModel = cfg.providerModels[ProviderId.OPEN_AI] ?: DEFAULT_OPENAI_MODEL,
				httpClient = httpClient,
				json = json
			)
		)
	}

	val geminiKey = when (val credential = cfg.credentials[ProviderId.GEMINI]) {
		is ProviderCredential.ApiKey -> credential.key
		is ProviderCredential.GoogleServiceJson -> extractGeminiKeyFromJson(credential.json)
		else -> null
	}
	geminiKey?.let {
		registerEngine(
			GeminiEngine(
				apiKeyProvider = { it },
				model = cfg.providerModels[ProviderId.GEMINI] ?: DEFAULT_GEMINI_MODEL,
				httpClient = httpClient,
				json = json
			)
		)
	}

	(cfg.credentials[ProviderId.ANTHROPIC] as? ProviderCredential.ApiKey)?.key?.let { anthropicKey ->
		registerEngine(
			AnthropicEngine(
				apiKeyProvider = { anthropicKey },
				model = cfg.providerModels[ProviderId.ANTHROPIC] ?: DEFAULT_ANTHROPIC_MODEL,
				httpClient = httpClient,
				json = json
			)
		)
	}

	(cfg.credentials[ProviderId.XAI] as? ProviderCredential.ApiKey)?.key?.let { grokKey ->
		registerEngine(
			GrokEngine(
				apiKeyProvider = { grokKey },
				model = cfg.providerModels[ProviderId.XAI] ?: DEFAULT_XAI_MODEL,
				httpClient = httpClient,
				json = json
			)
		)
	}
}

private fun extractGeminiKeyFromJson(jsonString: String): String? =
	runCatching {
		val jsonObject = JSONObject(jsonString)
		jsonObject.optString("api_key")
			.takeIf { it.isNotBlank() }
			?: jsonObject.optString("apiKey").takeIf { it.isNotBlank() }
	}.getOrNull()
