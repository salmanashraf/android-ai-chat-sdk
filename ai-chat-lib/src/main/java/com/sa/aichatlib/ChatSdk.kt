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
