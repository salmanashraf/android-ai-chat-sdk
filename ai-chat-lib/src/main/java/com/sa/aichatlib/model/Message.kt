package com.sa.aichatlib.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val message: String,
) {
    val isBot: Boolean = sender == "AI"
    companion object {
        fun defaultMessage(): Message = Message(sender = "AI", message = "Hi, I'm your assistant.")
    }
}
