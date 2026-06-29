package com.example.sftping.transfer.strategy

import com.example.sftping.sftp.ISftpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SftpTransferStrategy @Inject constructor(
    private val sftpClient: ISftpClient
) : TransferStrategy {

    override fun download(
        remotePath: String,
        localPath: String,
        skip: Long
    ): Flow<TransferProgress> = callbackFlow {
        sftpClient.downloadWithResume(remotePath, localPath, skip) { transferred, total ->
            trySend(TransferProgress(transferred, total))
        }
        close()
    }

    override fun upload(
        localPath: String,
        remotePath: String,
        totalBytes: Long,
        skip: Long
    ): Flow<TransferProgress> = callbackFlow {
        val onProgress: (Long, Long) -> Unit = { transferred, total ->
            trySend(TransferProgress(transferred, total))
        }
        // RESUME mode stat()s the remote file for its offset, which fails for a fresh
        // upload (remote file absent). Only resume when there is a real offset.
        if (skip > 0) {
            sftpClient.uploadWithResume(localPath, remotePath, skip, onProgress)
        } else {
            sftpClient.upload(localPath, remotePath, onProgress)
        }
        close()
    }
}
