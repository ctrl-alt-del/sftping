package com.example.sftping.sftp

import com.example.sftping.security.InMemoryKnownHostsStore
import com.jcraft.jsch.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class JschSftpClientTest {

    @Test
    fun `makeRemoteFile from root folder`() {
        val file = JschSftpClient.makeRemoteFile(
            parentPath = "/", fileName = "home", size = 4096, mTime = 1712928000, isDir = true
        )
        assertEquals("home", file.name)
        assertEquals("/home", file.path)
        assertEquals(-1L, file.size)
        assertEquals(1712928000L, file.lastModified)
        assertTrue(file.isDirectory)
    }

    @Test
    fun `makeRemoteFile from nested folder`() {
        val file = JschSftpClient.makeRemoteFile(
            parentPath = "/home/ops", fileName = "backups", size = 0, mTime = 1712928000, isDir = true
        )
        assertEquals("backups", file.name)
        assertEquals("/home/ops/backups", file.path)
        assertEquals(-1L, file.size)
    }

    @Test
    fun `makeRemoteFile regular file`() {
        val file = JschSftpClient.makeRemoteFile(
            parentPath = "/var/log", fileName = "syslog", size = 318_000_000, mTime = 1712928000, isDir = false
        )
        assertEquals("syslog", file.name)
        assertEquals("/var/log/syslog", file.path)
        assertEquals(318_000_000L, file.size)
        assertFalse(file.isDirectory)
    }

    @Test
    fun `makeRemoteFile root path with single slash`() {
        val file = JschSftpClient.makeRemoteFile(
            parentPath = "/", fileName = "etc", size = 0, mTime = 0, isDir = true
        )
        assertEquals("/etc", file.path)
    }

    @Test
    fun `openChannel throws and flips connected to false when session is non-null but dead`() {
        val sessionState = SessionState()
        sessionState.setConnected(true)
        val client = JschSftpClient(InMemoryKnownHostsStore(), sessionState)

        // Inject a non-null session that reports isConnected == false (dead session).
        val deadSession = mock<Session> { on { isConnected } doReturn false }
        val field = JschSftpClient::class.java.getDeclaredField("session")
        field.isAccessible = true
        field.set(client, deadSession)

        try {
            // openChannel() is private — invoke via homeDirectory() which calls it.
            kotlinx.coroutines.test.runTest {
                client.homeDirectory()
            }
        } catch (_: Exception) {
            // Expected — the session is dead.
        }

        // After detecting a dead session, connected should be flipped to false.
        assertFalse(sessionState.connected.value)
    }
}
