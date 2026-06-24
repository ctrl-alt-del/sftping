package com.example.sftping.sftp

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteFileTest {

    @Test
    fun `formatFileSize with negative returns dash`() {
        assertEquals("—", RemoteFile.formatFileSize(-1))
    }

    @Test
    fun `formatFileSize zero`() {
        assertEquals("0 B", RemoteFile.formatFileSize(0))
    }

    @Test
    fun `formatFileSize bytes`() {
        assertEquals("1023 B", RemoteFile.formatFileSize(1023))
        assertEquals("1 B", RemoteFile.formatFileSize(1))
    }

    @Test
    fun `formatFileSize kilobytes`() {
        assertEquals("1.0 KB", RemoteFile.formatFileSize(1024))
        assertEquals("1.5 KB", RemoteFile.formatFileSize(1536))
        assertEquals("1023.5 KB", RemoteFile.formatFileSize((1023.5 * 1024).toLong()))
    }

    @Test
    fun `formatFileSize megabytes`() {
        assertEquals("1.0 MB", RemoteFile.formatFileSize(1024 * 1024))
        assertEquals("5.5 MB", RemoteFile.formatFileSize((5.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formatFileSize gigabytes`() {
        assertEquals("1.0 GB", RemoteFile.formatFileSize(1024L * 1024 * 1024))
        assertEquals("1.4 GB", RemoteFile.formatFileSize((1.4 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formattedSize property returns formatted string`() {
        val file = RemoteFile("test.bin", "/home/test.bin", 1048576, 0, false)
        assertEquals("1.0 MB", file.formattedSize)
    }
}
