package com.sa.aichatlib.viewmodel

import com.sa.aichatlib.model.Message

data class ChatUiState(
	val messages: List<Message> = emptyList(),
	val composerText: String = "",
	val isSending: Boolean = false,
	val errorMessage: String? = null
)
