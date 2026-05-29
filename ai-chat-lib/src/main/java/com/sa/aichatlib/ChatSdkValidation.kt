package com.sa.aichatlib

import com.sa.aichatlib.provider.ProviderCredential
import com.sa.aichatlib.provider.ProviderId
import org.json.JSONObject

data class ChatSdkValidationResult(
	val provider: ProviderId,
	val model: String,
	val errors: List<String>
) {
	val isValid: Boolean = errors.isEmpty()
}

fun ChatSdkConfig.validateDefaultProvider(): ChatSdkValidationResult {
	val errors = mutableListOf<String>()
	val model = providerModels[defaultProvider] ?: defaultModelFor(defaultProvider)
	val credential = credentials[defaultProvider]

	if (model.isBlank()) {
		errors += "Model is required for ${defaultProvider.displayName()}."
	}

	when (defaultProvider) {
		ProviderId.OPEN_AI,
		ProviderId.ANTHROPIC,
		ProviderId.XAI -> {
			val apiKey = (credential as? ProviderCredential.ApiKey)?.key
			if (apiKey.isNullOrBlank()) {
				errors += "${defaultProvider.displayName()} API key is required."
			}
		}
		ProviderId.GEMINI -> {
			when (credential) {
				is ProviderCredential.ApiKey -> {
					if (credential.key.isBlank()) {
						errors += "Gemini API key is required."
					}
				}
				is ProviderCredential.GoogleServiceJson -> {
					if (extractGeminiApiKey(credential.json).isNullOrBlank()) {
						errors += "Gemini google.json must include api_key or apiKey."
					}
				}
				null -> errors += "Gemini API key or google.json is required."
			}
		}
	}

	return ChatSdkValidationResult(
		provider = defaultProvider,
		model = model,
		errors = errors
	)
}

fun ChatSdkConfig.requireValidDefaultProvider(): ChatSdkConfig {
	val validation = validateDefaultProvider()
	require(validation.isValid) {
		validation.errors.joinToString(separator = " ")
	}
	return this
}

internal fun extractGeminiApiKey(jsonString: String): String? =
	runCatching {
		val jsonObject = JSONObject(jsonString)
		jsonObject.optString("api_key")
			.takeIf { it.isNotBlank() }
			?: jsonObject.optString("apiKey").takeIf { it.isNotBlank() }
	}.getOrNull()
