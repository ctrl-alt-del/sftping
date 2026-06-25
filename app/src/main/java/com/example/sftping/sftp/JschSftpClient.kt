package com.example.sftping.sftp

import com.example.sftping.security.Fingerprint
import com.example.sftping.security.KnownHostsStore
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.SftpException as JschSftpException
import com.jcraft.jsch.SftpProgressMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Vector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JschSftpClient @Inject constructor(
    private val knownHosts: KnownHostsStore
) : ISftpClient {

    private var session: com.jcraft.jsch.Session? = null
    private var channel: ChannelSftp? = null

    override suspend fun connect(
        host: String, port: Int, user: String, password: String?
    ): HostKeyResult = withContext(Dispatchers.IO) {
        disconnectInternal()
        val jsch = JSch()
        session = jsch.getSession(user, host, port).apply {
            setPassword(password)
            setConfig("StrictHostKeyChecking", "no")
            setConfig("PreferredAuthentications", "password,keyboard-interactive")
            setServerAliveInterval(30_000)
            connect(10_000)
        }
        val hostKey = session?.hostKey ?: throw SftpException("No host key received")
        val keyBytes = java.util.Base64.getDecoder().decode(hostKey.key)
        val fingerprint = Fingerprint.sha256(keyBytes)
        val stored = knownHosts.get(host)

        when {
            stored == null -> HostKeyResult.Unknown(
                host = host, fingerprint = fingerprint, keyType = hostKey.type
            )
            stored == fingerprint -> {
                openSftpChannel()
                HostKeyResult.Trusted
            }
            else -> HostKeyResult.Changed(
                host = host, storedFingerprint = stored, presentedFingerprint = fingerprint
            )
        }
    }

    override suspend fun trustAndProceed(host: String) = withContext(Dispatchers.IO) {
        val hostKey = session?.hostKey ?: throw SftpException("No session to trust")
        val keyBytes = java.util.Base64.getDecoder().decode(hostKey.key)
        val fingerprint = Fingerprint.sha256(keyBytes)
        knownHosts.put(host, fingerprint)
        openSftpChannel()
    }

    private fun openSftpChannel() {
        channel = (session?.openChannel("sftp") as? ChannelSftp)?.apply {
            connect(10_000)
        } ?: throw SftpException("Failed to open SFTP channel")
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
        withContext(Dispatchers.IO) { disconnectInternal() }
    }

    override suspend fun delete(path: String) = withContext(Dispatchers.IO) {
        val ch = checkChannel()
        try {
            val stat = ch.stat(path)
            if (stat.isDir) {
                deleteRecursive(ch, path)
            } else {
                ch.rm(path)
            }
        } catch (e: JschSftpException) {
            throw SftpException("Failed to delete $path", e)
        }
    }

    override suspend fun rename(oldPath: String, newPath: String) = withContext(Dispatchers.IO) {
        val ch = checkChannel()
        try {
            ch.rename(oldPath, newPath)
        } catch (e: JschSftpException) {
            throw SftpException("Failed to rename $oldPath", e)
        }
    }

    override suspend fun download(
        remotePath: String, destFilePath: String, onProgress: (Long, Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        val ch = checkChannel()
        try {
            ch.get(remotePath, destFilePath, ProgressAdapter(onProgress), ChannelSftp.OVERWRITE)
        } catch (e: JschSftpException) {
            throw SftpException("Failed to download $remotePath", e)
        }
    }

    override suspend fun upload(
        srcFilePath: String, remotePath: String, onProgress: (Long, Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        val ch = checkChannel()
        try {
            ch.put(srcFilePath, remotePath, ProgressAdapter(onProgress), ChannelSftp.OVERWRITE)
        } catch (e: JschSftpException) {
            throw SftpException("Failed to upload $srcFilePath", e)
        }
    }
    override suspend fun downloadWithResume(
        remotePath: String, destFilePath: String, skip: Long,
        onProgress: (Long, Long) -> Unit
    ) = notYet()

    override suspend fun uploadWithResume(
        srcFilePath: String, remotePath: String, skip: Long,
        onProgress: (Long, Long) -> Unit
    ) = notYet()

    private fun notYet(): Nothing = throw UnsupportedOperationException("Not implemented yet")
    private fun checkChannel(): ChannelSftp = channel ?: throw IllegalStateException("Not connected")

    private fun deleteRecursive(ch: ChannelSftp, path: String) {
        @Suppress("UNCHECKED_CAST")
        val entries = ch.ls(path) as Vector<ChannelSftp.LsEntry>
        for (entry in entries) {
            val name = entry.filename
            if (name == "." || name == "..") continue
            val childPath = if (path == "/") "/$name" else "$path/$name"
            if (entry.attrs.isDir) {
                deleteRecursive(ch, childPath)
            } else {
                ch.rm(childPath)
            }
        }
        ch.rmdir(path)
    }

    private class ProgressAdapter(
        private val onProgress: (Long, Long) -> Unit
    ) : SftpProgressMonitor {
        private var max = 0L
        override fun init(op: Int, src: String?, dest: String?, max: Long) { this.max = max }
        override fun count(count: Long): Boolean { onProgress(count, max); return true }
        override fun end() {}
    }

    private fun disconnectInternal() {
        channel?.disconnect()
        channel = null
        session?.disconnect()
        session = null
    }

    companion object {
        fun ChannelSftp.LsEntry.toRemoteFile(parentPath: String): RemoteFile {
            val attrs = attrs
            return makeRemoteFile(parentPath, filename, attrs.size, attrs.mTime, attrs.isDir)
        }

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
