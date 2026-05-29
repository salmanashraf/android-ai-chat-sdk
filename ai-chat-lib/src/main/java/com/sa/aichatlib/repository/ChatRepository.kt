package com.sa.aichatlib.repository

import com.sa.aichatlib.ChatSdkConfig
import com.sa.aichatlib.dao.MessageDao
import com.sa.aichatlib.model.Message
import com.sa.aichatlib.provider.ChatRequest
import com.sa.aichatlib.provider.ChatResult
import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.provider.ChatRole
import com.sa.aichatlib.provider.LLMEngine
import com.sa.aichatlib.provider.ProviderId
import com.sa.aichatlib.provider.ProviderRegistry
import com.sa.aichatlib.utils.toChatPayload
import com.sa.aichatlib.utils.toDomain
import com.sa.aichatlib.utils.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatRepository(
	private val dao: MessageDao,
	private val providerRegistry: ProviderRegistry,
	private var config: ChatSdkConfig
) {
	val messages: Flow<List<Message>> = dao.getAllMessages().map { list ->
		list.map { it.toDomain() }
	}

	suspend fun insert(message: Message) = dao.insert(message.toEntity())
	suspend fun clear() = dao.clear()

	fun updateConfig(newConfig: ChatSdkConfig) {
		config = newConfig
	}

	suspend fun getAIResponse(
		personaPrompt: String? = null,
		usePersona: Boolean? = null
	): String = withContext(Dispatchers.IO) {
		val history = dao.getMessagesOnce().map { it.toDomain().toChatPayload() }
		respond(
			messages = history,
			personaPrompt = personaPrompt,
			usePersona = usePersona,
			persist = false
		)
	}

	suspend fun respond(
		prompt: String,
		personaPrompt: String? = null,
		usePersona: Boolean? = null,
		useHistory: Boolean? = null,
		persist: Boolean? = null
	): String = withContext(Dispatchers.IO) {
		val resolvedUsePersona = usePersona ?: config.usePersonaByDefault
		val resolvedUseHistory = useHistory ?: config.persistHistoryForHeadless
		val resolvedPersist = persist ?: config.persistHistoryForHeadless
		val persona = resolvePersonaPrompt(personaPrompt, resolvedUsePersona)

		val history = if (resolvedUseHistory) {
			dao.getMessagesOnce().map { it.toDomain().toChatPayload() }
		} else {
			emptyList()
		}
		val payload = buildPayloadWithPersona(
			base = history + ChatMessagePayload(ChatRole.USER, prompt),
			personaPrompt = persona
		)

		if (resolvedPersist) {
			insert(Message(sender = "User", message = prompt))
		}

		val response = complete(payload)
		if (resolvedPersist) {
			insert(Message(sender = "AI", message = response))
		}
		response
	}

	suspend fun respond(
		messages: List<ChatMessagePayload>,
		personaPrompt: String? = null,
		usePersona: Boolean? = null,
		persist: Boolean? = null
	): String = withContext(Dispatchers.IO) {
		val resolvedUsePersona = usePersona ?: config.usePersonaByDefault
		val resolvedPersist = persist ?: config.persistHistoryForHeadless
		val persona = resolvePersonaPrompt(personaPrompt, resolvedUsePersona)
		val payload = buildPayloadWithPersona(base = messages, personaPrompt = persona)

		val response = complete(payload)
		if (resolvedPersist) {
			val lastUser = messages.lastOrNull { it.role == ChatRole.USER }?.content
			if (!lastUser.isNullOrBlank()) {
				insert(Message(sender = "User", message = lastUser))
			}
			insert(Message(sender = "AI", message = response))
		}
		response
	}

	private suspend fun complete(messages: List<ChatMessagePayload>): String {
		val selection = selectProvider()
			?: return "No provider configured. Please apply a provider key."
		val request = ChatRequest(
			providerId = selection.id,
			messages = messages,
			model = config.providerModels[selection.id]
		)

		return when (val result = selection.engine.complete(request)) {
			is ChatResult.Success -> result.content
			is ChatResult.Error -> result.throwable.message ?: "Provider error"
		}
	}

	private fun resolvePersonaPrompt(personaPrompt: String?, usePersona: Boolean): String? {
		if (!usePersona) return null
		val candidate = personaPrompt?.takeIf { it.isNotBlank() } ?: config.defaultPersonaPrompt
		return candidate?.takeIf { it.isNotBlank() }
	}

	private fun buildPayloadWithPersona(
		base: List<ChatMessagePayload>,
		personaPrompt: String?
	): List<ChatMessagePayload> {
		if (personaPrompt.isNullOrBlank()) return base
		return listOf(ChatMessagePayload(ChatRole.SYSTEM, personaPrompt.trim())) + base
	}

	private data class ProviderSelection(val id: ProviderId, val engine: LLMEngine)

	private fun selectProvider(): ProviderSelection? {
		val resolvedProvider = when {
			providerRegistry.hasProvider(config.defaultProvider) -> config.defaultProvider
			else -> providerRegistry.all().firstOrNull()?.providerId
		} ?: return null

		val engine = providerRegistry.get(resolvedProvider) ?: return null
		return ProviderSelection(resolvedProvider, engine)
	}
}
