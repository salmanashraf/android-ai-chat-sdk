package com.sa.aichatlib.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenAIChatRequest(
	val model: String,
	val messages: List<OpenAIMessage>
)

@Serializable
data class OpenAIMessage(
	val role: String,
	val content: String
)

@Serializable
data class OpenAIChatResponse(
	val choices: List<Choice>
)

@Serializable
data class Choice(
	val message: OpenAIMessage
)
