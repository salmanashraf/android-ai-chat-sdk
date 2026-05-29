package com.sa.aichatlib.provider

class ProviderRegistry {
	private val engines = linkedMapOf<ProviderId, LLMEngine>()

	fun register(engine: LLMEngine) {
		engines[engine.providerId] = engine
	}

	fun get(providerId: ProviderId): LLMEngine? = engines[providerId]

	fun all(): List<LLMEngine> = engines.values.toList()

	fun hasProvider(providerId: ProviderId): Boolean = engines.containsKey(providerId)

	fun clear() {
		engines.clear()
	}
}
