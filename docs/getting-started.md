# Getting Started

This guide walks you through integrating the AI Chat library into your Android app.

## Requirements
- Android 7.0+ (API 24+)
- Kotlin 2.0+
- Jetpack Compose recommended
- Coroutine support

## Installation

Add the Maven Central dependency:

```gradle
implementation("io.github.salmanashraf:aichatlib:1.0.2")
```

## Configure the SDK

The direct API-key setup below is useful for demos and local validation. For production mobile apps, prefer a backend proxy so provider keys are not embedded in the APK or app bundle. See [security.md](security.md).

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

## UI Integration (Compose)

```kotlin
setContent {
    MaterialTheme {
        ChatScreen(
            personaPrompt = "You are a helpful assistant.",
            usePersona = true
        )
    }
}
```

To use a generic chat, pass `usePersona = false` or omit the persona prompt.

## Headless Integration (No UI)

```kotlin
val response = ChatSdk.client().respond(
    prompt = "Write a short bio about Ada Lovelace.",
    personaPrompt = "You are a concise biographer.",
    usePersona = true
)
```

The headless API is stateless by default. To include and persist history:

```kotlin
val response = ChatSdk.client().respond(
    prompt = "Continue the conversation.",
    useHistory = true,
    persist = true
)
```
