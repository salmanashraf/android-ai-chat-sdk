package com.sa.aichatlib.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sa.aichatlib.model.Message
import com.sa.aichatlib.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val personaPrompt: String? = null,
    private val usePersona: Boolean? = null
) : ViewModel()  {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()


    init {
        loadMessagesFromDb()
    }

    private fun loadMessagesFromDb() {
        viewModelScope.launch {
            repository.messages.collect { messageList ->
                _uiState.update { it.copy(messages = messageList) }
            }
        }
    }


    fun sendUserMessage(text: String) {
        updateComposerText(text)
        sendMessage()
    }

    fun updateComposerText(value: String) {
        _uiState.update { it.copy(composerText = value) }
    }

    fun sendMessage() {
        val trimmed = _uiState.value.composerText.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, composerText = "") }
            try {
                val userMessage = Message(sender = "User", message = trimmed)
                repository.insert(userMessage)

                val aiReply = repository.getAIResponse(
                    personaPrompt = personaPrompt,
                    usePersona = usePersona
                )
                repository.insert(Message(sender = "AI", message = aiReply))

                _uiState.update { it.copy(isSending = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, errorMessage = e.message ?: "Unable to send message") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clear()
        }
    }
}
