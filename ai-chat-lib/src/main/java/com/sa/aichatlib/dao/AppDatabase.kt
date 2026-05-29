package com.sa.aichatlib.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
	abstract fun messageDao(): MessageDao
}
