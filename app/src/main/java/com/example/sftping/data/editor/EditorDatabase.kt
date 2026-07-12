package com.example.sftping.data.editor

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PendingEdit::class], version = 1, exportSchema = false)
abstract class EditorDatabase : RoomDatabase() {
    abstract fun pendingEditDao(): PendingEditDao
}
