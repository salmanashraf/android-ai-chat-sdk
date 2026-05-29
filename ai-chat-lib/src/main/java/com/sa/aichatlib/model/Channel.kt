package com.sa.aichatlib.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Channel(
    val id: String = UUID.randomUUID().toString(),
    val messages: List<Message>
)
