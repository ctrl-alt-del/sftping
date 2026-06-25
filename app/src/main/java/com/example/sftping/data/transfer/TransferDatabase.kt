package com.example.sftping.data.transfer

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TransferTask::class], version = 1, exportSchema = false)
abstract class TransferDatabase : RoomDatabase() {
    abstract fun transferTaskDao(): TransferTaskDao
}
