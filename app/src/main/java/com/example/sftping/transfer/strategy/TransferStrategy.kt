package com.example.sftping.transfer.strategy

import kotlinx.coroutines.flow.Flow

interface TransferStrategy {
    fun download(
        remotePath: String,
        localPath: String,
        skip: Long = 0L
    ): Flow<TransferProgress>

    fun upload(
        localPath: String,
        remotePath: String,
        totalBytes: Long,
        skip: Long = 0L
    ): Flow<TransferProgress>
}
