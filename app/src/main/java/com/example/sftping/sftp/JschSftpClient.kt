package com.example.sftping.sftp

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.SftpException as JschSftpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Vector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JschSftpClient @Inject constructor() : ISftpClient {

    private var session: com.jcraft.jsch.Session? = null
    private var channel: ChannelSftp? = null

    override suspend fun connect(host: String, port: Int, user: String, password: String?) {
        withContext(Dispatchers.IO) {
            disconnectInternal()
            val jsch = JSch()
            session = jsch.getSession(user, host, port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                setConfig("PreferredAuthentications", "password,keyboard-interactive")
                setServerAliveInterval(30_000)
                connect(10_000)
            }
            channel = (session?.openChannel("sftp") as? ChannelSftp)?.apply {
                connect(10_000)
            } ?: throw SftpException("Failed to open SFTP channel")
        }
    }

    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val ch = checkChannel()
        try {
            @Suppress("UNCHECKED_CAST")
            val entries = ch.ls(path) as Vector<ChannelSftp.LsEntry>
            entries
                .filter { !it.filename.matches(Regex("^\\.\\.?$")) }
                .map { it.toRemoteFile(path) }
        } catch (e: JschSftpException) {
            throw SftpException("Failed to list files at $path", e)
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            disconnectInternal()
        }
    }

    override suspend fun delete(path: String) = notYet()
    override suspend fun rename(oldPath: String, newPath: String) = notYet()
    override suspend fun download(remotePath: String, destFilePath: String, onProgress: (Long, Long) -> Unit) = notYet()
    override suspend fun upload(srcFilePath: String, remotePath: String, onProgress: (Long, Long) -> Unit) = notYet()
    override suspend fun downloadWithResume(
        remotePath: String, destFilePath: String, skip: Long,
        onProgress: (Long, Long) -> Unit
    ) = notYet()

    override suspend fun uploadWithResume(
        srcFilePath: String, remotePath: String, skip: Long,
        onProgress: (Long, Long) -> Unit
    ) = notYet()

    private fun notYet(): Nothing = throw UnsupportedOperationException("Not implemented yet")

    private fun checkChannel(): ChannelSftp =
        channel ?: throw IllegalStateException("Not connected. Call connect() first.")

    private fun disconnectInternal() {
        channel?.disconnect()
        channel = null
        session?.disconnect()
        session = null
    }

    companion object {
        fun ChannelSftp.LsEntry.toRemoteFile(parentPath: String): RemoteFile = makeRemoteFile(
            parentPath = parentPath,
            fileName = filename,
            size = attrs.size,
            mTime = attrs.mTime,
            isDir = attrs.isDir
        )

        fun makeRemoteFile(
            parentPath: String,
            fileName: String,
            size: Long,
            mTime: Int,
            isDir: Boolean
        ): RemoteFile = RemoteFile(
            name = fileName,
            path = if (parentPath == "/") "/$fileName" else "$parentPath/$fileName",
            size = if (isDir) -1L else size,
            lastModified = mTime.toLong(),
            isDirectory = isDir
        )
    }
}
