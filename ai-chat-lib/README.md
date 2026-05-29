# AI Chat SDK for Android

This Android library adds a Compose chat UI, Room-backed message history, and a headless response API for apps that want to integrate OpenAI, Gemini, Anthropic, or Grok through a shared SDK surface.

## Features

- Jetpack Compose UI for chat
- OpenAI, Gemini, Anthropic, and Grok provider engines
- Local message persistence using Room
- Real-time chat updates using Kotlin Flow
- Persona prompt support
- Headless API for response-only usage
- Sample app included

## Installation

```gradle
implementation("io.github.salmanashraf:aichatlib:1.0.2")
```

## Project Structure

```
android-ai-chat-sdk/
├── ai-chat-lib/              # Reusable Android SDK module
├── sampleapp/                # Demo app using the SDK
└── docs/                     # Integration and roadmap docs
```

## Configure Providers

```kotlin
ChatSdk.initializeWithDefaults(
    context = applicationContext,
    config = ChatSdkConfig(
        defaultProvider = ProviderId.OPEN_AI,
        credentials = mapOf(
            ProviderId.OPEN_AI to ProviderCredential.ApiKey("sk-...")
        )
    )
)
```

## UI Usage

```kotlin
setContent {
    MaterialTheme {
        ChatScreen(
            personaPrompt = "You are a friendly fitness coach.",
            usePersona = true
        )
    }
}
```

To use a generic chat, pass `usePersona = false` or omit the persona prompt.

## Headless Usage

```kotlin
val response = ChatSdk.client().respond(
    prompt = "Give me a 3-day workout plan.",
    personaPrompt = "You are a concise personal trainer.",
    usePersona = true
)
```

To keep the call stateless, leave `useHistory` and `persist` unset (defaults to false).

## Roadmap

Tools, embeddings, RAG examples, encrypted local storage, streaming responses, and advanced provider capabilities are roadmap items. They should not be treated as shipped SDK features yet.

## License

MIT - Free to use, modify, and extend.
