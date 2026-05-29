# AI Chat SDK – Multi-Provider Expansion Plan

This is a roadmap document. It describes planned architecture and future capabilities, not the full set of features currently shipped by `ai-chat-lib`.

## Current Shipped Scope
- Single Android library module: `ai-chat-lib`.
- Compose chat UI and headless response API.
- Room-backed local message persistence.
- Provider engines for OpenAI, Gemini, Anthropic, and Grok.
- Sample app for manual provider configuration and testing.

## 1. Vision & Outcomes
- Deliver a Jetpack Compose/Kotlin-first SDK where app teams can switch between GPT (OpenAI), Claude (Anthropic), Gemini (Google), and Grok (xAI) without code rewrites.
- Keep the onboarding friction low: consumers add the library, initialize once, inject their own API keys, and start rendering the chat UI.
- Move toward stronger security posture, modular architecture, documentation, and roadmap governance.

## 2. Target Architecture
- Keep the current `ai-chat-lib` artifact as the public integration point.
- Consider splitting internals into dedicated core, UI, provider, tools, embeddings, and crypto modules only when the added separation reduces consumer friction.
- Keep `sampleapp` as the runnable demo for provider switching and credential entry.

## 3. Provider Abstraction
```kotlin
interface LLMEngine {
    val id: ProviderId
    suspend fun chat(request: ChatRequest): Flow<ChatResponseChunk>
    fun supports(feature: ProviderFeature): Boolean
}

interface ProviderRegistry {
    fun register(engine: LLMEngine)
    fun resolve(providerId: ProviderId): LLMEngine
}
```
- Each provider module maps SDK requests to its REST/gRPC protocol, normalises responses into streaming chunks, and handles retry/backoff logic.

## 4. Integration Flow for App Developers
1. Add Gradle dependency to `ai-chat-lib`.
2. Initialize the SDK in `Application`:
   ```kotlin
   ChatSdk.init(
       context = this,
       config = ChatSdkConfig(
           defaultProvider = ProviderId.OpenAI,
           credentials = mapOf(
               ProviderId.OpenAI to ApiKeyCredential("<OPENAI_KEY>"),
               ProviderId.Gemini to ApiKeyCredential("<GEMINI_KEY>")
           )
       )
   )
   ```
3. Render `ChatScreen()` in Compose; switch providers by updating `ChatSessionConfig`.
4. Future versions may add optional tools and embeddings engines.

## 5. Configuration & Key Management
- Support `ChatSdkConfig` sourced from:
  - `credentials.properties` (sample app only).
  - Runtime injection (recommended): host app fetches keys from its secure store and passes them to `ChatSdk`.
- Provide `ProviderCredentialResolver` interface so enterprise adopters can inject their own vault lookup.
- Keys are never stored in plaintext inside the SDK; only kept in memory per session.

## 6. Provider Implementation Roadmap
| Phase | Provider | Key Tasks |
| --- | --- | --- |
| 1 | GPT (OpenAI) | Stream responses via `chat/completions`, function-call support, retries, error mapping. |
| 2 | Gemini | REST client (Generative Language API), safety settings, streaming tokens. |
| 3 | Claude | Implement Anthropic Messages API with tool-use support. |
| 4 | Grok (xAI) | Wire REST endpoints, align JSON schema, add brand-specific metadata. |
| 5 | Fallback | Add transparent provider failover + health checks. |

## 7. Tooling & Plugin System Roadmap
- Define `ChatTool` interface (`suspend fun invoke(context: ToolContext): ToolResult`).
- Provide registries for pre-processor, post-processor, and contextual tools.
- Document sample tools (web search, calendar) and show registration inside the sample app.

## 8. Offline Embeddings Roadmap
- `EmbeddingEngine` contract with `TFLiteEmbeddingEngine` and `OnnxEmbeddingEngine`.
- Vector store backed by Room FTS or SQLite, enabling semantic recall and RAG examples.
- Allow host apps to opt-in via `ChatSdkConfig.embeddings`.

## 9. Security & Compliance Roadmap
- Add AES-256-GCM encryption for stored messages using Android Keystore.
- HTTP clients enforce TLS 1.2+, optional certificate pinning.
- Secrets injection only through runtime config; provide lint rule that flags hardcoded keys.
- Structured logging with redaction, configurable telemetry hooks for enterprises.
- Provide a threat model appendix in docs.

## 10. Testing & Quality Strategy
- Unit tests per provider using MockWebServer + golden fixtures.
- Contract tests verifying schema compatibility when APIs change (WireMock).
- Compose UI tests covering streaming, provider switching, error banners.
- Integration tests simulating multi-provider conversations + plugin executions.
- Performance baselines for latency and memory.
- CI/CD via GitHub Actions (lint, detekt, ktlint, unit/instrumentation, sample build, docs build).

## 11. Documentation Package
- Update root `README` with provider matrix, quickstart, configuration table, security summary.
- `/docs/providers/*.md` – setup & limitations for GPT, Claude, Gemini, Grok.
- `/docs/tools/authoring-guide.md`, `/docs/security.md`, `/docs/testing.md`.
- `/docs/roadmap/v1.1-v1.5.md` – detail upcoming iterations.

## 12. Implementation Phases & Milestones
1. **Foundation (Weeks 1–2)** – Module scaffolding, DI setup, provider registry, OpenAI adapter, Compose UI refresh.
2. **Multi-provider Core (Weeks 3–4)** – Gemini + Claude modules, credential resolver, runtime configs, sample app provider switcher.
3. **Advanced Features (Weeks 5–6)** – Tool SDK, embeddings module, Grok adapter, offline cache.
4. **Security & Compliance (Week 7)** – Encryption, lint rules, logging guardrails, documentation updates.
5. **Stabilization (Week 8)** – Full test suite, CI matrix, demo polishing, v1.1 release.

## 13. Success Metrics
- Time-to-integrate < 30 minutes (verified through onboarding checklist).
- 90% unit/integration test coverage for provider modules.
- 0 plaintext secrets in repo (verified by CI secret scanner).
- Sample app demonstrates all four providers with user-supplied keys.
