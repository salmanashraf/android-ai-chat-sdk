# High-Level Architecture

AI Chat SDK for Android is split into three main layers: app integration, SDK core, and provider engines. Apps can either use the bundled Compose UI or call the headless client directly.

## System Diagram

```mermaid
flowchart TB
    app["Host Android App"] --> init["ChatSdk.configure / initializeWithDefaults"]
    app --> ui["ChatScreen (Compose UI)"]
    app --> headless["ChatSdk.client() (Headless API)"]

    init --> config["ChatSdkConfig"]
    init --> registry["ProviderRegistry"]
    init --> db["Room Database"]

    ui --> vm["ChatViewModel"]
    vm --> repo["ChatRepository"]
    headless --> client["ChatClient"]
    client --> repo

    repo --> history["MessageDao / Local History"]
    history --> db
    repo --> registry

    registry --> openai["OpenAiEngine"]
    registry --> gemini["GeminiEngine"]
    registry --> anthropic["AnthropicEngine"]
    registry --> xai["GrokEngine"]
    registry --> custom["Custom LLMEngine"]

    openai --> openaiApi["OpenAI API"]
    gemini --> geminiApi["Gemini API"]
    anthropic --> anthropicApi["Anthropic API"]
    xai --> xaiApi["xAI API"]
    custom --> backend["Your Backend / Custom Provider"]
```

## Layer Responsibilities

| Layer | Main Classes | Responsibility |
| --- | --- | --- |
| App integration | `ChatSdk`, `ChatSdkConfig`, `ChatSdkConfigBuilder` | Initialize the SDK, select provider, pass credentials, choose models, update config at runtime. |
| Compose UI | `ChatScreen`, `ChatViewModel`, UI components | Render chat, collect user input, show messages, loading state, and typing indicator. |
| Headless API | `ChatClient` | Let apps request model responses without using the bundled UI. |
| Repository | `ChatRepository` | Build chat payloads, apply persona prompts, choose provider, persist history when requested. |
| Provider runtime | `ProviderRegistry`, `LLMEngine`, `ChatRequest`, `ChatResult` | Register engines and route requests to the selected provider. |
| Provider engines | `OpenAiEngine`, `GeminiEngine`, `AnthropicEngine`, `GrokEngine` | Translate SDK requests into provider-specific HTTP payloads and parse responses. |
| Persistence | `AppDatabase`, `MessageDao`, `MessageEntity` | Store and stream local chat history through Room. |

## Request Flow With Compose UI

```mermaid
sequenceDiagram
    participant User
    participant ChatScreen
    participant ChatViewModel
    participant ChatRepository
    participant ProviderRegistry
    participant Engine as Selected LLMEngine
    participant Provider as AI Provider API
    participant Room

    User->>ChatScreen: Type message and send
    ChatScreen->>ChatViewModel: sendMessage(text)
    ChatViewModel->>ChatRepository: respond / getAIResponse
    ChatRepository->>Room: Save or load messages
    ChatRepository->>ProviderRegistry: Resolve default provider
    ProviderRegistry-->>ChatRepository: Selected engine
    ChatRepository->>Engine: complete(ChatRequest)
    Engine->>Provider: HTTP request
    Provider-->>Engine: Provider response
    Engine-->>ChatRepository: ChatResult.Success / Error
    ChatRepository->>Room: Save AI response
    ChatRepository-->>ChatViewModel: Response text
    ChatViewModel-->>ChatScreen: Updated UI state
```

## Request Flow With Headless API

```mermaid
sequenceDiagram
    participant App
    participant Client as ChatSdk.client()
    participant ChatRepository
    participant Engine as Selected LLMEngine
    participant Provider as AI Provider API

    App->>Client: respond(prompt)
    Client->>ChatRepository: respond(...)
    ChatRepository->>Engine: complete(ChatRequest)
    Engine->>Provider: HTTP request
    Provider-->>Engine: Provider response
    Engine-->>ChatRepository: ChatResult
    ChatRepository-->>Client: Response text
    Client-->>App: Response text
```

## Configuration Model

`ChatSdkConfig` controls:

- `defaultProvider`: active provider.
- `credentials`: provider credentials such as API keys or Gemini `google.json`.
- `providerModels`: model ID per provider.
- `defaultPersonaPrompt`: optional default system/persona prompt.
- `usePersonaByDefault`: whether to use the default persona automatically.
- `persistHistoryForHeadless`: whether headless calls should read/write local history.
- `databaseName`: Room database name.

Example:

```kotlin
ChatSdk.configure(applicationContext) {
    defaultProvider = ProviderId.OPEN_AI
    openAI(BuildConfig.OPENAI_KEY, model = "gpt-4.1")
}
```

## Provider Extensibility

The SDK routes all model calls through `LLMEngine`. Built-in engines are registered by `ChatSdk.initializeWithDefaults(...)` and `ChatSdk.applyConfig(...)`.

Apps can provide their own engine for a backend proxy or unsupported provider:

```kotlin
class BackendChatEngine(
    private val api: BackendChatApi
) : LLMEngine {
    override val providerId = ProviderId.OPEN_AI

    override suspend fun complete(request: ChatRequest): ChatResult {
        return runCatching {
            api.complete(request.messages, request.model).reply
        }.fold(
            onSuccess = { ChatResult.Success(it) },
            onFailure = { ChatResult.Error(it) }
        )
    }
}
```

Register it:

```kotlin
ChatSdk.initialize(applicationContext)
ChatSdk.registerEngine(BackendChatEngine(api))
```

## Production Deployment Shape

For public apps, provider API keys should live on your backend, not inside the APK.

```mermaid
flowchart LR
    android["Android App"] --> sdk["AI Chat SDK"]
    sdk --> backend["Your Backend Proxy"]
    backend --> auth["Auth / Rate Limits / Abuse Checks"]
    auth --> provider["AI Provider API"]
```

This lets the mobile app use the same SDK UI and headless API while your backend controls credentials, allowed models, quotas, and provider-specific policy.

## Current Boundaries

- The bundled UI is Compose-first.
- Direct provider keys are intended for demos, internal apps, and local validation.
- Streaming responses are not yet part of the shipped SDK.
- Tools, RAG, embeddings, and encrypted local storage are roadmap items.
