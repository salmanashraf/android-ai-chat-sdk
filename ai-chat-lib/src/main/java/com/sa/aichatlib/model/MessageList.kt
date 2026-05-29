package com.sa.aichatlib.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageList(
  val messages: List<Message>,
)
