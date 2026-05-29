package com.sa.aichatlib.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val sender: String,
	val content: String,
	val timestamp: Long = System.currentTimeMillis()
)
