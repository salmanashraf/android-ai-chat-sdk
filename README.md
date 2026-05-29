# AI Chat SDK for Android

Multi-provider AI chat SDK for Android apps. Add a ready-made Jetpack Compose chat UI or call the same providers from a headless Kotlin API.

The SDK currently supports OpenAI, Gemini, Claude/Anthropic, and Grok/xAI through one configuration surface.

## What You Get

- Ready-to-use Compose chat screen with message bubbles, loading state, and typing indicator.
- Headless API for apps that need AI responses without the bundled UI.
- Runtime provider switching with OpenAI, Gemini, Claude/Anthropic, and Grok/xAI.
- Room-backed local message history.
- Persona/system prompt support.
- Provider configuration validation before opening chat.
- Sample app for testing provider keys and models.

## Installation

Add the Maven Central dependency:

```gradle
implementation("io.github.salmanashraf:aichatlib:1.0.4")
```

Minimum requirements:

- Android 7.0+ / API 24+
- Kotlin 2.0+
- Jetpack Compose for the bundled chat UI

## Quick Start

Initialize the SDK once, usually from your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        ChatSdk.configure(this) {
            defaultProvider = ProviderId.OPEN_AI
            openAI(BuildConfig.OPENAI_KEY)
        }
    }
}
```

Show the chat UI in any Compose screen:

```kotlin
ChatScreen()
```

Use the headless API when you do not want the bundled UI:

```kotlin
val reply = ChatSdk.client().respond(
    prompt = "Write a short welcome message for my app."
)
```

## Provider Setup

### OpenAI

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.OPEN_AI
    openAI(
        apiKey = BuildConfig.OPENAI_KEY,
        model = "gpt-4.1"
    )
}
```

### Gemini

Use an API key:

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.GEMINI
    gemini(
        apiKey = BuildConfig.GEMINI_KEY,
        model = "models/gemini-2.5-flash"
    )
}
```

Or pass a `google.json` string:

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.GEMINI
    geminiServiceJson(
        json = googleJson,
        model = "models/gemini-2.5-flash"
    )
}
```

### Claude / Anthropic

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.ANTHROPIC
    anthropic(
        apiKey = BuildConfig.ANTHROPIC_KEY,
        model = "claude-sonnet-4-20250514"
    )
}
```

### Grok / xAI

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.XAI
    xAI(
        apiKey = BuildConfig.XAI_KEY,
        model = "grok-4.3"
    )
}
```

## Validate Configuration

If users enter provider keys inside your app, validate the active provider before opening chat:

```kotlin
val validation = ChatSdk.config().validateDefaultProvider()

if (!validation.isValid) {
    showError(validation.errors.joinToString("\n"))
}
```

For fail-fast setup:

```kotlin
ChatSdk.config().requireValidDefaultProvider()
```

## Switch Providers At Runtime

```kotlin
ChatSdk.applyConfig(
    ChatSdkConfig(
        defaultProvider = ProviderId.GEMINI,
        credentials = mapOf(
            ProviderId.GEMINI to ProviderCredential.ApiKey(BuildConfig.GEMINI_KEY)
        ),
        providerModels = mapOf(
            ProviderId.GEMINI to "models/gemini-2.5-flash"
        )
    )
)
```

## Persona Prompts

Use a persona/system prompt with the bundled UI:

```kotlin
ChatScreen(
    personaPrompt = "You are a concise support assistant.",
    usePersona = true
)
```

Use a persona prompt with the headless API:

```kotlin
val reply = ChatSdk.client().respond(
    prompt = "Explain this error to a beginner.",
    personaPrompt = "You are a patient Android mentor.",
    usePersona = true
)
```

## Production Security

Do not ship unrestricted provider API keys inside a production APK or app bundle.

Direct API key configuration is useful for demos, internal tools, and local validation. Production apps should call your own backend. The backend should own provider credentials, authenticate users, enforce rate limits, and return only the model response to the mobile app.

See [docs/security.md](docs/security.md) for the recommended backend-provider pattern.

## Common Errors

### `ChatSdk.initialize must be called`

Initialize the SDK before calling `ChatScreen()` or `ChatSdk.client()`:

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.OPEN_AI
    openAI(BuildConfig.OPENAI_KEY)
}
```

### `No provider configured`

The selected provider does not have a registered engine. Use `ChatSdk.configure(...)`, `ChatSdk.initializeWithDefaults(...)`, or register a custom `LLMEngine`.

### Missing API Key

Call `validateDefaultProvider()` and show `validation.errors` to the user before opening chat.

### Gemini `google.json` Not Working

The JSON must include either `api_key` or `apiKey`. The sample app reads `sampleapp/src/main/assets/google.json` automatically when Gemini is selected.

### Dependency Not Resolving

Confirm Maven Central is enabled:

```gradle
repositories {
    mavenCentral()
}
```

## Sample App

The `sampleapp` module demonstrates:

- Provider selection.
- Model selection.
- API key entry.
- Gemini `google.json` loading.
- Compose chat UI.
- Headless prompt testing.

Run it from Android Studio with the `sampleapp` configuration.

## Project Structure

| Module | Description |
| --- | --- |
| `ai-chat-lib` | Published Android library with SDK core, provider engines, Compose UI, and Room persistence. |
| `sampleapp` | Demo app for manual testing and provider switching. |
| `docs` | Integration, security, roadmap, testing, and publishing docs. |

## Documentation

- [High-level architecture](docs/architecture.md)
- [Getting started](docs/getting-started.md)
- [Security guide](docs/security.md)
- [Development test guide](docs/DEV_TEST_GUIDE.md)
- [Publishing guide](docs/publishing.md)
- [Provider roadmap](docs/MULTI_PROVIDER_PLAN.md)

## Current Limitations

- The bundled UI is Compose-first.
- Production apps should use a backend proxy instead of direct mobile provider keys.
- Streaming responses are not yet shipped.
- Advanced tools, RAG, and embeddings are roadmap items.

## License

[MIT](https://github.com/salmanashraf/android-ai-chat-sdk/blob/master/LICENSE)
