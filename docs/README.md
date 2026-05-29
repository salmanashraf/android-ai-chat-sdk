# AI Chat Android SDK
Multi-provider AI chat framework for Android (Jetpack Compose + Kotlin).  
Supports OpenAI (GPT), Claude, Gemini, Grok, and custom LLMs.

## Features
- Plug-and-play Compose Chat UI
- Multi-provider switching (GPT, Claude, Gemini, Grok)
- Headless API for response-only usage
- Persona prompt support (system message)
- Room-backed local message persistence
- Demo app included

## Installation

```gradle
implementation("io.github.salmanashraf:aichatlib:1.0.2")
```

## Documentation
Full docs available in `/docs`.

Start here: [getting-started.md](getting-started.md)

Security guidance: [security.md](security.md)

## Quick Start
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

ChatScreen(personaPrompt = "You are a travel guide.", usePersona = true)
```

Headless response-only:

```kotlin
val reply = ChatSdk.client().respond(
    prompt = "Plan a 2-day Tokyo trip.",
    personaPrompt = "You are a concise travel guide.",
    usePersona = true
)
```
