package com.sa.aichatlib.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sa.aichatlib.repository.ChatRepository
import com.sa.aichatlib.viewmodel.ChatViewModel

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory(
	private val repository: ChatRepository,
	private val personaPrompt: String? = null,
	private val usePersona: Boolean? = null
) :
	ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return ChatViewModel(
			repository = repository,
			personaPrompt = personaPrompt,
			usePersona = usePersona
		) as T
	}
}
