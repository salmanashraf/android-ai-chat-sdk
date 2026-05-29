package com.sa.aichatlib.utils

import com.sa.aichatlib.dao.MessageEntity
import com.sa.aichatlib.model.Message
import com.sa.aichatlib.provider.ChatMessagePayload
import com.sa.aichatlib.provider.ChatRole

fun MessageEntity.toDomain(): Message =
	Message(
		id = id.toString(), // or generate a UUID if you prefer
		sender = sender,
		message = content
	)

fun Message.toEntity(): MessageEntity =
	MessageEntity(
		sender = sender,
		content = message,
		timestamp = System.currentTimeMillis()
	)

fun Message.toChatPayload(): ChatMessagePayload =
	ChatMessagePayload(
		role = if (isBot) ChatRole.ASSISTANT else ChatRole.USER,
		content = message
	)
