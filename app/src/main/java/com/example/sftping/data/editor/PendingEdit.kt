package com.example.sftping.data.editor

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A locally-cached, not-yet-synced edit for a remote file, keyed by its remote
 * path. Persists across process death so offline edits are never lost; flushed
 * to the server on reconnect.
 */
@Entity(tableName = "pending_edits")
data class PendingEdit(
    @PrimaryKey @ColumnInfo(name = "remote_path") val remotePath: String,
    val content: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
