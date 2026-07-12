package com.example.sftping.data.editor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingEditDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(edit: PendingEdit)

    @Query("SELECT * FROM pending_edits WHERE remote_path = :remotePath")
    suspend fun get(remotePath: String): PendingEdit?

    @Query("SELECT * FROM pending_edits")
    suspend fun all(): List<PendingEdit>

    @Query("SELECT remote_path FROM pending_edits")
    fun observePendingPaths(): Flow<List<String>>

    @Query("DELETE FROM pending_edits WHERE remote_path = :remotePath")
    suspend fun delete(remotePath: String)
}
