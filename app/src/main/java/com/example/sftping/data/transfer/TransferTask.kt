package com.example.sftping.data.transfer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_tasks")
data class TransferTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "remote_path") val remotePath: String,
    @ColumnInfo(name = "local_uri") val localUri: String = "",
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "total_bytes") val totalBytes: Long,
    @ColumnInfo(name = "transferred_bytes") val transferredBytes: Long,
    val direction: TransferTaskDirection,
    val status: TransferTaskStatus,
    @ColumnInfo(name = "last_modified") val lastModified: Long = System.currentTimeMillis()
)

enum class TransferTaskDirection { DOWNLOAD, UPLOAD }
enum class TransferTaskStatus { PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED }
