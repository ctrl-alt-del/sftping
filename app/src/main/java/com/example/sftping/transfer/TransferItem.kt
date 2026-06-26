package com.example.sftping.transfer

import com.example.sftping.data.transfer.TransferTaskDirection

data class TransferItem(
    val id: Long,
    val fileName: String,
    val remotePath: String,
    val direction: TransferDirection,
    val totalBytes: Long,
    val transferredBytes: Long,
    val status: TransferStatus,
    val speed: Long = 0L
)

enum class TransferDirection { 
    DOWNLOAD, 
    UPLOAD;
    
    fun toTaskDirection() = when (this) {
        DOWNLOAD -> TransferTaskDirection.DOWNLOAD
        UPLOAD -> TransferTaskDirection.UPLOAD
    }
}

enum class TransferStatus { RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED }
