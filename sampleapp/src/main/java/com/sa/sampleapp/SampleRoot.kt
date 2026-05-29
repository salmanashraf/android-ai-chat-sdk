package com.sa.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sa.aichatlib.ChatSdk
import com.sa.aichatlib.ChatSdkConfig
import com.sa.aichatlib.applyConfig
import com.sa.aichatlib.provider.ProviderCredential
import com.sa.aichatlib.provider.ProviderId
import com.sa.aichatlib.ui.ChatScreen
import kotlinx.coroutines.launch

@Composable
fun SampleRoot() {
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()

	// Values persisted across rotations.
	var selectedProvider by rememberSaveable { mutableStateOf(ProviderId.OPEN_AI) }
	var openAiKey by rememberSaveable { mutableStateOf("") }
	var geminiKey by rememberSaveable { mutableStateOf("") }
	var anthropicKey by rememberSaveable { mutableStateOf("") }
	var grokKey by rememberSaveable { mutableStateOf("") }
	var headlessPrompt by rememberSaveable { mutableStateOf("") }
	var headlessPersona by rememberSaveable { mutableStateOf("") }
	var headlessResponse by rememberSaveable { mutableStateOf("") }
	var headlessLoading by remember { mutableStateOf(false) }
	var uiPersona by rememberSaveable { mutableStateOf("") }
	var uiUsePersona by rememberSaveable { mutableStateOf(false) }
	var showAdvanced by rememberSaveable { mutableStateOf(false) }
	val context = LocalContext.current

	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
	) { padding ->
		Column(
			modifier = Modifier
				.padding(padding)
				.fillMaxSize()
		) {
			ProviderConfigCard(
				selectedProvider = selectedProvider,
				onProviderChange = { selectedProvider = it },
				openAiKey = openAiKey,
				onOpenAiChange = { openAiKey = it },
				geminiKey = geminiKey,
				onGeminiChange = { geminiKey = it },
				anthropicKey = anthropicKey,
				onAnthropicChange = { anthropicKey = it },
				grokKey = grokKey,
				onGrokChange = { grokKey = it },
				onApply = {
					val credentials = buildMap {
						if (openAiKey.isNotBlank()) put(ProviderId.OPEN_AI, ProviderCredential.ApiKey(openAiKey.trim()))
						loadGeminiCredential(context, geminiKey)?.let { put(ProviderId.GEMINI, it) }
						if (anthropicKey.isNotBlank()) put(ProviderId.ANTHROPIC, ProviderCredential.ApiKey(anthropicKey.trim()))
						if (grokKey.isNotBlank()) put(ProviderId.XAI, ProviderCredential.ApiKey(grokKey.trim()))
					}
					if (credentials[selectedProvider] == null) {
						scope.launch {
							snackbarHostState.showSnackbar("Configure a credential for $selectedProvider before applying")
						}
						return@ProviderConfigCard
					}
					ChatSdk.applyConfig(
						ChatSdkConfig(
							defaultProvider = selectedProvider,
							credentials = credentials
						)
					)
					scope.launch {
						snackbarHostState.showSnackbar("Applied config for $selectedProvider")
					}
				}
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(text = "Demo tools")
				Spacer(modifier = Modifier.weight(1f))
				TextButton(onClick = { showAdvanced = !showAdvanced }) {
					Text(if (showAdvanced) "Hide" else "Show")
				}
			}

			if (showAdvanced) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.heightIn(max = 320.dp)
						.verticalScroll(rememberScrollState())
				) {
					HeadlessDemoCard(
						prompt = headlessPrompt,
						onPromptChange = { headlessPrompt = it },
						persona = headlessPersona,
						onPersonaChange = { headlessPersona = it },
						response = headlessResponse,
						loading = headlessLoading,
						onSend = {
							if (headlessPrompt.isBlank()) return@HeadlessDemoCard
							headlessLoading = true
							scope.launch {
								val reply = ChatSdk.client().respond(
									prompt = headlessPrompt.trim(),
									personaPrompt = headlessPersona.takeIf { it.isNotBlank() },
									usePersona = headlessPersona.isNotBlank()
								)
								headlessResponse = reply
								headlessLoading = false
							}
						}
					)
					UiPersonaCard(
						persona = uiPersona,
						onPersonaChange = { uiPersona = it },
						usePersona = uiUsePersona,
						onUsePersonaChange = { uiUsePersona = it }
					)
				}
			}

			HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

			Card(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.padding(horizontal = 12.dp, vertical = 4.dp)
			) {
				ChatScreen(
					personaPrompt = uiPersona.takeIf { it.isNotBlank() },
					usePersona = uiUsePersona
				)
			}
		}
	}
}

@Composable
private fun ProviderConfigCard(
	selectedProvider: ProviderId,
	onProviderChange: (ProviderId) -> Unit,
	openAiKey: String,
	onOpenAiChange: (String) -> Unit,
	geminiKey: String,
	onGeminiChange: (String) -> Unit,
	anthropicKey: String,
	onAnthropicChange: (String) -> Unit,
	grokKey: String,
	onGrokChange: (String) -> Unit,
	onApply: () -> Unit
) {
	var dropdownExpanded by remember { mutableStateOf(false) }
	val options = listOf(
		ProviderId.OPEN_AI to "OpenAI (GPT)",
		ProviderId.GEMINI to "Gemini",
		ProviderId.ANTHROPIC to "Claude",
		ProviderId.XAI to "Grok"
	)

	Card(
		modifier = Modifier
			.padding(horizontal = 12.dp, vertical = 8.dp)
			.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(12.dp)) {
			Text(text = "Provider & Credentials")
			Spacer(modifier = Modifier.height(6.dp))
			Text(text = "Default Provider")
			OutlinedButton(
				onClick = { dropdownExpanded = true },
				modifier = Modifier.fillMaxWidth()
			) {
				Text(options.first { it.first == selectedProvider }.second)
			}
			DropdownMenu(
				expanded = dropdownExpanded,
				onDismissRequest = { dropdownExpanded = false }
			) {
				options.forEach { (id, label) ->
					DropdownMenuItem(
						text = { Text(label) },
						onClick = {
							onProviderChange(id)
							dropdownExpanded = false
						}
					)
				}
			}

			Spacer(modifier = Modifier.height(8.dp))
			KeyFieldForProvider(
				provider = selectedProvider,
				openAiKey = openAiKey,
				onOpenAiChange = onOpenAiChange,
				geminiKey = geminiKey,
				onGeminiChange = onGeminiChange,
				anthropicKey = anthropicKey,
				onAnthropicChange = onAnthropicChange,
				grokKey = grokKey,
				onGrokChange = onGrokChange
			)

			Spacer(modifier = Modifier.height(8.dp))
			Button(
				onClick = onApply,
				modifier = Modifier.align(Alignment.End)
			) {
				Text("Apply")
			}
		}
	}
}

@Composable
private fun UiPersonaCard(
	persona: String,
	onPersonaChange: (String) -> Unit,
	usePersona: Boolean,
	onUsePersonaChange: (Boolean) -> Unit
) {
	Card(
		modifier = Modifier
			.padding(horizontal = 12.dp)
			.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text(text = "Chat UI Persona")
			Spacer(modifier = Modifier.height(8.dp))
			TextField(
				value = persona,
				onValueChange = onPersonaChange,
				modifier = Modifier.fillMaxWidth(),
				label = { Text("Persona (optional)") }
			)
			Spacer(modifier = Modifier.height(12.dp))
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(text = "Use persona")
				Spacer(modifier = Modifier.weight(1f))
				Switch(
					checked = usePersona,
					onCheckedChange = onUsePersonaChange
				)
			}
		}
	}
}

@Composable
private fun HeadlessDemoCard(
	prompt: String,
	onPromptChange: (String) -> Unit,
	persona: String,
	onPersonaChange: (String) -> Unit,
	response: String,
	loading: Boolean,
	onSend: () -> Unit
) {
	Card(
		modifier = Modifier
			.padding(horizontal = 12.dp)
			.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text(text = "Headless SDK (No UI)")
			Spacer(modifier = Modifier.height(8.dp))
			TextField(
				value = prompt,
				onValueChange = onPromptChange,
				modifier = Modifier.fillMaxWidth(),
				label = { Text("Prompt") }
			)
			Spacer(modifier = Modifier.height(8.dp))
			TextField(
				value = persona,
				onValueChange = onPersonaChange,
				modifier = Modifier.fillMaxWidth(),
				label = { Text("Persona (optional)") }
			)
			Spacer(modifier = Modifier.height(12.dp))
			Button(
				onClick = onSend,
				enabled = !loading,
				modifier = Modifier.align(Alignment.End)
			) {
				Text(if (loading) "Sending..." else "Send")
			}
			if (response.isNotBlank()) {
				Spacer(modifier = Modifier.height(12.dp))
				HorizontalDivider()
				Spacer(modifier = Modifier.height(8.dp))
				Text(text = "Response")
				Spacer(modifier = Modifier.height(4.dp))
				Text(text = response)
			}
		}
	}
}

@Composable
private fun KeyFieldForProvider(
	provider: ProviderId,
	openAiKey: String,
	onOpenAiChange: (String) -> Unit,
	geminiKey: String,
	onGeminiChange: (String) -> Unit,
	anthropicKey: String,
	onAnthropicChange: (String) -> Unit,
	grokKey: String,
	onGrokChange: (String) -> Unit
) {
	when (provider) {
		ProviderId.OPEN_AI -> ApiKeyField("OpenAI API Key", openAiKey, onOpenAiChange)
		ProviderId.GEMINI -> Column {
			Text(text = "Place google.json under sampleapp/src/main/assets or paste API key.")
			Spacer(modifier = Modifier.height(4.dp))
			ApiKeyField("Gemini API Key (optional when google.json provided)", geminiKey, onGeminiChange)
		}
		ProviderId.ANTHROPIC -> ApiKeyField("Anthropic API Key", anthropicKey, onAnthropicChange)
		ProviderId.XAI -> ApiKeyField("Grok API Key", grokKey, onGrokChange)
	}
}

@Composable
private fun ApiKeyField(label: String, value: String, onValueChange: (String) -> Unit) {
	TextField(
		value = value,
		onValueChange = onValueChange,
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		singleLine = true,
		label = { Text(label) }
	)
}

private fun loadGeminiCredential(context: android.content.Context, apiKey: String): ProviderCredential? {
	if (apiKey.isNotBlank()) return ProviderCredential.ApiKey(apiKey.trim())
	val assetName = "google.json"
	return runCatching {
		context.assets.open(assetName).bufferedReader().use { it.readText() }
	}.getOrNull()?.takeIf { it.isNotBlank() }?.let { ProviderCredential.GoogleServiceJson(it) }
}
