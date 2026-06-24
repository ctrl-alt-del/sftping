package com.example.sftping.sftp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
