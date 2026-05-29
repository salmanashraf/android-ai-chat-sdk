# CODEX - Persona + Headless SDK Plan (Updated)

## Goal
Support two usage modes in the same SDK:
- UI mode: Compose chat UI with optional persona prompt or generic chat.
- Headless mode: response-only API for developers who build their own UI.

## Final Decisions
- No server API contract needed; use existing providers (OpenAI, Gemini, Anthropic, XAI).
- Persona prompt is optional and injected as a SYSTEM message when enabled.
- Headless calls are stateless by default (no history, no persistence).
- Headless calls can optionally include/persist history via flags.

## Implemented Changes

1) Public API surface
- Added `ChatSdk.client()` to access the headless API.
- Added `ChatClient` with:
  - `respond(prompt: String, personaPrompt: String? = null, usePersona: Boolean? = null, useHistory: Boolean? = null, persist: Boolean? = null)`
  - `respond(messages: List<ChatMessagePayload>, personaPrompt: String? = null, usePersona: Boolean? = null, persist: Boolean? = null)`
- Updated `ChatSdkConfig` with:
  - `defaultPersonaPrompt`
  - `usePersonaByDefault`
  - `persistHistoryForHeadless`

2) Repository changes
- Added a unified headless response path in `ChatRepository`.
- `getAIResponse()` now routes through the shared response path.
- Supports optional history inclusion and persistence.

3) Persona handling
- Persona prompt becomes a `SYSTEM` message when enabled.
- Generic chat is used when persona is missing or disabled.
- Call-time override supported even when defaults exist.

4) UI flow updates
- `ChatScreen` accepts `personaPrompt` and `usePersona`.
- `ChatViewModel` and `ChatViewModelFactory` pass persona to repository.

5) Documentation and samples
- Updated `ai-chat-lib/README.md`, `docs/README.md`, `docs/getting-started.md`.
- Added headless demo card in the sample app.

## Not Implemented (Explicitly)
- No custom server-backed provider.
- No new tests were added yet.

## Open Items (Optional)
- Add unit tests for headless response + persona handling.
- Add a provider mock for deterministic test responses.
