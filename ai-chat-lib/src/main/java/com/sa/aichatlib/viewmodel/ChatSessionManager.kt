package com.sa.aichatlib.viewmodel

import androidx.lifecycle.ViewModel
import com.sa.aichatlib.model.Channel
import com.sa.aichatlib.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatSessionManager : ViewModel() {
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    fun createChannel(): Channel {
        val newChannel = Channel(messages = listOf(Message.defaultMessage()))
        _channels.value += newChannel
        return newChannel
    }
}