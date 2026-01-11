# AI Chat Android Framework

Production-grade, multi-provider AI chat SDK for Android written in Kotlin + Jetpack Compose. The framework exposes a modular architecture (core, UI, providers, tools, embeddings, crypto) so enterprise teams can mix-and-match LLM providers (OpenAI/GPT, Claude, Gemini, Grok) while keeping UX, persistence, and security consistent with Tech Nation expectations.

## Features
- **Plug-and-play Compose UI** with chat bubbles, loading states, and typing indicators exposed via `ChatScreen`.
- **Multi-provider runtime** powered by `ChatSdk` + `ProviderRegistry`, supporting OpenAI, Gemini, Anthropic (Claude), Grok (xAI); pluggable via `LLMEngine`.
- **Runtime configuration API** allowing apps to switch providers/credentials without restarts (`ChatSdk.applyConfig`).
- **Room persistence** for chat history with coroutine flows feeding Compose.
- **Sample app** showcasing provider selection & credential entry for manual testing.
- **Extensible modules** for tools, embeddings (TF Lite/ONNX), crypto, etc., per Tech Nation roadmap.

## Quickstart
1. Clone the repo and open in Android Studio Giraffe+.
2. Provide API keys (and a Gemini `google.json`) using the sample app config card:
   - Place `google.json` under `sampleapp/src/main/assets/`.
   - Run `sampleapp` and use the dropdown to pick a provider, enter the key if needed, and tap **Apply**.
3. To embed in your app:
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
4. Use Compose UI in any screen: `ChatScreen()` automatically hooks into the shared `ChatViewModel`.

## Module Overview
| Module | Description |
| --- | --- |
| `ai-chat-lib` | Distribution artifact that exposes UI + core functionality. |
| `modules/core` | Domain + repositories + Room + provider contracts. |
| `modules/ui` | Compose components, theming, view models. |
| `modules/providers/*` | Engines for OpenAI, Gemini, Anthropic, XAI. |
| `modules/tools` | Tooling/plugin SDK stubs. |
| `modules/embeddings` | Placeholder for TF Lite/ONNX embeddings integration. |
| `modules/crypto` | Placeholder for AES-256/Keystore abstractions. |
| `sampleapp` | Demo app for Tech Nation showcase/testing. |

## Provider Configuration
- OpenAI / Claude / Grok: API key only.
- Gemini: API key or `google.json` file (place in `sampleapp/src/main/assets/google.json` so `ChatSdk` can read it).
- Call `ChatSdk.applyConfig(ChatSdkConfig(...))` whenever you need to switch providers at runtime; the SDK rebuilds its provider registry and repository automatically.


## Contributing
1. Read `docs/UPGRADE_PLAN.md` + `docs/MULTI_PROVIDER_PLAN.md`.
2. Run lint/tests before opening a PR.
3. Provide screenshots for UI changes and include provider test notes.
