package com.sa.aichatlib.provider

enum class ProviderId {
    OPEN_AI,
    GEMINI,
    ANTHROPIC,
    XAI
}

sealed interface ProviderCredential {
    data class ApiKey(val key: String) : ProviderCredential
    data class GoogleServiceJson(val json: String) : ProviderCredential
}
