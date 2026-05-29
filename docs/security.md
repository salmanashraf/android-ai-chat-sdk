# Security Guide

This SDK can call model providers directly when configured with `ProviderCredential.ApiKey`. That mode is convenient for demos, local testing, and internal prototypes, but it is usually not appropriate for production mobile apps.

## Do Not Ship Provider Keys In Production Apps

Mobile binaries can be inspected. If an unrestricted OpenAI, Anthropic, Gemini, or xAI key is packaged into an APK or app bundle, assume it can be extracted and abused.

Avoid storing provider keys in:
- `BuildConfig`
- checked-in Gradle files
- Android resources or assets
- client-side encrypted preferences
- remote config values readable by every app install

## Recommended Production Pattern

Use a backend service as the model gateway:

1. The Android app authenticates the user with your backend.
2. The app sends chat messages to your backend over HTTPS.
3. The backend validates the user, applies rate limits, and checks product entitlements.
4. The backend calls OpenAI, Gemini, Anthropic, xAI, or another provider using server-held credentials.
5. The backend returns the response to the Android app.

This keeps provider credentials out of the mobile client and gives you a place to enforce abuse controls, audit logs, request shaping, and cost limits.

## Using The SDK With A Backend

For production, register a custom `LLMEngine` that calls your backend instead of a public model provider directly:

```kotlin
class BackendChatEngine(
    private val api: YourBackendApi
) : LLMEngine {
    override val providerId = ProviderId.OPEN_AI

    override suspend fun complete(request: ChatRequest): ChatResult {
        return runCatching {
            api.completeChat(request.messages)
        }.fold(
            onSuccess = { ChatResult.Success(it.message) },
            onFailure = { ChatResult.Error(it) }
        )
    }
}

ChatSdk.initialize(context)
ChatSdk.registerEngine(BackendChatEngine(api))
```

Use the built-in direct provider engines only when direct client-side credentials are acceptable for your threat model.

## Minimum Backend Controls

- Authenticate every request.
- Rate-limit by user, device, and account.
- Keep provider keys in server-side secret storage.
- Validate model names and request sizes server-side.
- Redact secrets and user-sensitive data from logs.
- Track provider spend and enforce quotas.
- Return typed errors that the app can display safely.

## Local Storage

The SDK currently uses Room-backed local message persistence. Treat local chat history as app data and decide whether your product needs additional encryption, retention limits, or user-controlled deletion.
