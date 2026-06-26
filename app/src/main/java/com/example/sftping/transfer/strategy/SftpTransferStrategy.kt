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
        sftpClient.uploadWithResume(localPath, remotePath, skip) { transferred, total ->
            trySend(TransferProgress(transferred, total))
        }
        close()
    }
}
