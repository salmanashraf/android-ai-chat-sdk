# AI Chat Android Framework

Multi-provider AI chat SDK for Android written in Kotlin + Jetpack Compose. The current package ships a reusable `ai-chat-lib` Android library plus a sample app, with Compose UI, Room-backed message persistence, and provider engines for OpenAI, Gemini, Anthropic, and Grok.

## Features
- **Plug-and-play Compose UI** with chat bubbles, loading states, and typing indicators exposed via `ChatScreen`.
- **Multi-provider runtime** powered by `ChatSdk` + `ProviderRegistry`, supporting OpenAI, Gemini, Anthropic (Claude), Grok (xAI); pluggable via `LLMEngine`.
- **Runtime configuration API** allowing apps to switch providers/credentials without restarts (`ChatSdk.applyConfig`).
- **Room persistence** for chat history with coroutine flows feeding Compose.
- **Sample app** showcasing provider selection & credential entry for manual testing.
- **Single Android library artifact** via `io.github.salmanashraf:aichatlib`.

## Quickstart
1. Add the Maven Central dependency:
   ```gradle
   implementation("io.github.salmanashraf:aichatlib:1.0.4")
   ```
2. Initialize the SDK from your `Application` class:
   ```kotlin
   class MyApp : Application() {
       override fun onCreate() {
           super.onCreate()
           val config = ChatSdkConfig(
               defaultProvider = ProviderId.OPEN_AI,
               credentials = mapOf(
                   ProviderId.OPEN_AI to ProviderCredential.ApiKey(BuildConfig.OPENAI_KEY)
               )
           )
           ChatSdk.initializeWithDefaults(this, config)
       }
   }
   ```
   Or use the builder API:
   ```kotlin
   ChatSdk.configure(this) {
       defaultProvider = ProviderId.OPEN_AI
       openAI(BuildConfig.OPENAI_KEY)
   }
   ```
   You can validate credentials before opening chat:
   ```kotlin
   val validation = ChatSdk.config().validateDefaultProvider()
   if (!validation.isValid) {
       // Show validation.errors to the user.
   }
   ```
3. Use Compose UI in any screen:
   ```kotlin
   ChatScreen()
   ```

## Running the Sample App
1. Clone the repo and open it in Android Studio.
2. Provide API keys (and a Gemini `google.json`) using the sample app config card:
   - Place `google.json` under `sampleapp/src/main/assets/`.
   - Run `sampleapp` and use the dropdown to pick a provider, enter the key if needed, and tap **Apply**.

## Project Structure
| Module | Description |
| --- | --- |
| `ai-chat-lib` | Distribution artifact that exposes UI + core functionality. |
| `sampleapp` | Demo app for manual provider testing. |

## Provider Configuration
- OpenAI / Claude / Grok: API key only.
- Gemini: API key or `google.json` file (place in `sampleapp/src/main/assets/google.json` so `ChatSdk` can read it).
- Call `ChatSdk.applyConfig(ChatSdkConfig(...))` whenever you need to switch providers at runtime; the SDK rebuilds its provider registry and repository automatically.

## Security
Do not ship unrestricted provider API keys in a production mobile app. The direct `ProviderCredential.ApiKey(...)` examples are suitable for demos, internal tools, and prototypes where the risk is understood. Production apps should route requests through a backend that owns provider credentials, applies auth and rate limits, and returns only the model response to the app.

See [docs/security.md](docs/security.md) for the recommended production pattern.

## Contributing
1. Read `docs/MULTI_PROVIDER_PLAN.md`.
2. Run lint/tests before opening a PR.
3. Provide screenshots for UI changes and include provider test notes.
