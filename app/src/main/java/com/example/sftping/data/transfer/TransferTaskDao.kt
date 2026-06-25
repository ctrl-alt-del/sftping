package com.example.sftping.data.transfer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TransferTask): Long

    @Query("UPDATE transfer_tasks SET transferred_bytes = :bytes, status = :status WHERE id = :id")
    suspend fun updateProgress(id: Long, bytes: Long, status: TransferTaskStatus)

    @Query("UPDATE transfer_tasks SET total_bytes = :total WHERE id = :id")
    suspend fun updateTotal(id: Long, total: Long)

    @Query("UPDATE transfer_tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TransferTaskStatus)

    @Query("SELECT * FROM transfer_tasks ORDER BY last_modified DESC")
    suspend fun all(): List<TransferTask>

    @Query("SELECT * FROM transfer_tasks ORDER BY last_modified DESC")
    fun observeAll(): Flow<List<TransferTask>>

    @Query("SELECT * FROM transfer_tasks WHERE id = :id")
    suspend fun get(id: Long): TransferTask?

    @Query("DELETE FROM transfer_tasks WHERE id = :id")
    suspend fun delete(id: Long)
}
