package com.sa.aichatlib

import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.repository.ChatRepository

/**
 * Headless API for apps that want AI responses without the SDK UI.
 */
class ChatClient internal constructor(
	private val repository: ChatRepository
) {
	suspend fun respond(
		prompt: String,
		personaPrompt: String? = null,
		usePersona: Boolean? = null,
		useHistory: Boolean? = null,
		persist: Boolean? = null
	): String = repository.respond(
		prompt = prompt,
		personaPrompt = personaPrompt,
		usePersona = usePersona,
		useHistory = useHistory,
		persist = persist
	)

	suspend fun respond(
		messages: List<ChatMessagePayload>,
		personaPrompt: String? = null,
		usePersona: Boolean? = null,
		persist: Boolean? = null
	): String = repository.respond(
		messages = messages,
		personaPrompt = personaPrompt,
		usePersona = usePersona,
		persist = persist
	)
}
