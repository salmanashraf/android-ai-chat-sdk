package com.sa.aichatlib.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(message: MessageEntity)

	@Query("SELECT * FROM messages ORDER BY timestamp ASC")
	fun getAllMessages(): Flow<List<MessageEntity>>

	@Query("SELECT * FROM messages ORDER BY timestamp ASC")
	suspend fun getMessagesOnce(): List<MessageEntity>

	@Query("DELETE FROM messages")
	suspend fun clear()
}
