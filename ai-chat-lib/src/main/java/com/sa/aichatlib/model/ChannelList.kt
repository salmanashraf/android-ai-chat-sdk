package com.sa.aichatlib.model

import kotlinx.serialization.Serializable

@Serializable
data class ChannelList(
	val channels: List<Channel>,
)
