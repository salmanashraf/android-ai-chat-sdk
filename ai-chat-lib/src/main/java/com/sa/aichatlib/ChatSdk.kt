package com.sa.aichatlib

import android.content.Context
import androidx.room.Room
import com.sa.aichatlib.dao.AppDatabase
import com.sa.aichatlib.provider.LLMEngine
import com.sa.aichatlib.provider.ProviderCredential
import com.sa.aichatlib.provider.ProviderId
import com.sa.aichatlib.provider.ProviderRegistry
import com.sa.aichatlib.repository.ChatRepository

/**
 * Central SDK entry point used to share configuration and repositories across modules.
 */
object ChatSdk {
    private var state: ChatSdkState? = null

    @Synchronized
    fun initialize(
        context: Context,
        config: ChatSdkConfig = ChatSdkConfig(),
    ) {
        if (state != null) return
        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            config.databaseName
        ).build()
        val registry = ProviderRegistry()
        val repository = ChatRepository(
            dao = database.messageDao(),
            providerRegistry = registry,
            config = config
        )
        val client = ChatClient(repository)
        state = ChatSdkState(
            config = config,
            repository = repository,
            registry = registry,
            client = client
        )
    }

    fun requireRepository(): ChatRepository = state?.repository
        ?: error("ChatSdk.initialize must be called before using the chat UI")

    fun client(): ChatClient = state?.client
        ?: error("ChatSdk.initialize must be called before using the headless client")

    fun config(): ChatSdkConfig = state?.config
        ?: error("ChatSdk.initialize must be called before accessing configuration")

    fun providerRegistry(): ProviderRegistry = state?.registry
        ?: error("ChatSdk.initialize must be called before working with providers")

    fun registerEngine(engine: LLMEngine) = providerRegistry().register(engine)

    fun updateConfig(newConfig: ChatSdkConfig) {
        val currentState = state ?: error("ChatSdk not initialized")
        currentState.config = newConfig
        currentState.repository.updateConfig(newConfig)
        currentState.registry.clear()
    }

    fun configure(
        context: Context,
        block: ChatSdkConfigBuilder.() -> Unit
    ) {
        val config = ChatSdkConfigBuilder().apply(block).build()
        initializeWithDefaults(context, config)
    }
}

data class ChatSdkConfig(
    val defaultProvider: ProviderId = ProviderId.OPEN_AI,
    val databaseName: String = "chat_db",
    val credentials: Map<ProviderId, ProviderCredential> = emptyMap(),
    val providerModels: Map<ProviderId, String> = emptyMap(),
    val defaultPersonaPrompt: String? = null,
    val usePersonaByDefault: Boolean = false,
    val persistHistoryForHeadless: Boolean = false
)

class ChatSdkState(
    var config: ChatSdkConfig,
    val repository: ChatRepository,
    val registry: ProviderRegistry,
    val client: ChatClient
)

class ChatSdkConfigBuilder {
    var defaultProvider: ProviderId = ProviderId.OPEN_AI
    var databaseName: String = "chat_db"
    var defaultPersonaPrompt: String? = null
    var usePersonaByDefault: Boolean = false
    var persistHistoryForHeadless: Boolean = false

    private val credentials = linkedMapOf<ProviderId, ProviderCredential>()
    private val providerModels = linkedMapOf<ProviderId, String>()

    fun openAI(
        apiKey: String,
        model: String = DEFAULT_OPENAI_MODEL
    ) = provider(
        providerId = ProviderId.OPEN_AI,
        credential = ProviderCredential.ApiKey(apiKey),
        model = model
    )

    fun gemini(
        apiKey: String,
        model: String = DEFAULT_GEMINI_MODEL
    ) = provider(
        providerId = ProviderId.GEMINI,
        credential = ProviderCredential.ApiKey(apiKey),
        model = model
    )

    fun geminiServiceJson(
        json: String,
        model: String = DEFAULT_GEMINI_MODEL
    ) = provider(
        providerId = ProviderId.GEMINI,
        credential = ProviderCredential.GoogleServiceJson(json),
        model = model
    )

    fun anthropic(
        apiKey: String,
        model: String = DEFAULT_ANTHROPIC_MODEL
    ) = provider(
        providerId = ProviderId.ANTHROPIC,
        credential = ProviderCredential.ApiKey(apiKey),
        model = model
    )

    fun xAI(
        apiKey: String,
        model: String = DEFAULT_XAI_MODEL
    ) = provider(
        providerId = ProviderId.XAI,
        credential = ProviderCredential.ApiKey(apiKey),
        model = model
    )

    fun provider(
        providerId: ProviderId,
        credential: ProviderCredential,
        model: String = defaultModelFor(providerId)
    ) {
        credentials[providerId] = credential
        providerModels[providerId] = model
    }

    fun build(): ChatSdkConfig =
        ChatSdkConfig(
            defaultProvider = defaultProvider,
            databaseName = databaseName,
            credentials = credentials.toMap(),
            providerModels = providerModels.toMap(),
            defaultPersonaPrompt = defaultPersonaPrompt,
            usePersonaByDefault = usePersonaByDefault,
            persistHistoryForHeadless = persistHistoryForHeadless
        )
}
