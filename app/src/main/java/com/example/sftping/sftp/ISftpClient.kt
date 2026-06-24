package com.example.sftping.sftp

import java.io.IOException

interface ISftpClient {
    suspend fun connect(host: String, port: Int, user: String, password: String?): HostKeyResult

    suspend fun trustAndProceed(host: String)

    suspend fun listFiles(path: String): List<RemoteFile>

    suspend fun disconnect()

    suspend fun delete(path: String)

    suspend fun rename(oldPath: String, newPath: String)

    suspend fun download(remotePath: String, destFilePath: String, onProgress: (transferred: Long, total: Long) -> Unit)

    suspend fun upload(srcFilePath: String, remotePath: String, onProgress: (transferred: Long, total: Long) -> Unit)

    suspend fun downloadWithResume(
        remotePath: String,
        destFilePath: String,
        skip: Long,
        onProgress: (transferred: Long, total: Long) -> Unit
    )

    suspend fun uploadWithResume(
        srcFilePath: String,
        remotePath: String,
        skip: Long,
        onProgress: (transferred: Long, total: Long) -> Unit
    )
}

class SftpException(message: String, cause: Throwable? = null) : IOException(message, cause)
