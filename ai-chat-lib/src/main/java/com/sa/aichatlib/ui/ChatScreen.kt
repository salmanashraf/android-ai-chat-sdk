package com.sa.aichatlib.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sa.aichatlib.ChatSdk
import com.sa.aichatlib.factory.ChatViewModelFactory
import com.sa.aichatlib.ui.components.ChatToolbar
import com.sa.aichatlib.ui.components.ComposerBar
import com.sa.aichatlib.ui.components.MessageList
import com.sa.aichatlib.ui.theme.ChatTokens
import com.sa.aichatlib.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
	personaPrompt: String? = null,
	usePersona: Boolean? = null,
	viewModel: ChatViewModel = defaultChatViewModel(
		personaPrompt = personaPrompt,
		usePersona = usePersona
	)
) {
	val uiState by viewModel.uiState.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }

	LaunchedEffect(uiState.errorMessage) {
		val error = uiState.errorMessage ?: return@LaunchedEffect
		snackbarHostState.showSnackbar(error)
		viewModel.dismissError()
	}

	Scaffold(
		topBar = { ChatToolbar(title = "AI Chat", onClearHistory = viewModel::clearConversation) },
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
			) {
			MessageList(
				messages = uiState.messages,
				isTyping = uiState.isSending,
				typingLabel = "Gemini is thinking...",
				modifier = Modifier
					.weight(1f)
					.fillMaxSize()
					.padding(horizontal = ChatTokens.spacingSmall)
			)

			ComposerBar(
				value = uiState.composerText,
				onValueChange = { viewModel.updateComposerText(it) },
				onSend = {
					viewModel.sendMessage()
				},
				sendEnabled = uiState.composerText.trim().isNotEmpty(),
				modifier = Modifier.padding(
					horizontal = ChatTokens.spacingSmall,
					vertical = ChatTokens.spacingSmall
				)
			)
		}
	}
}

@Composable
fun defaultChatViewModel(
	personaPrompt: String? = null,
	usePersona: Boolean? = null
): ChatViewModel {
	return viewModel(
		factory = ChatViewModelFactory(
			repository = ChatSdk.requireRepository(),
			personaPrompt = personaPrompt,
			usePersona = usePersona
		)
	)
}
