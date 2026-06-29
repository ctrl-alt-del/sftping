package com.example.sftping.transfer.strategy

import com.example.sftping.sftp.ISftpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class SftpTransferStrategyTest {

    @Test
    fun `download emits TransferProgress from ISftpClient callback`() = runTest {
        val client = mock<ISftpClient>()
        doAnswer { invocation ->
            val onProgress = invocation.getArgument<(Long, Long) -> Unit>(3)
            onProgress(500, 1000)
        }.`when`(client).downloadWithResume(
            any(), any(), any(), any<(Long, Long) -> Unit>()
        )

        val strategy = SftpTransferStrategy(client)
        val progress = strategy.download("/remote", "/local", 0).first()

        assertEquals(500, progress.transferredBytes)
        assertEquals(1000, progress.totalBytes)
    }

    @Test
    fun `upload with skip zero emits progress via overwrite upload`() = runTest {
        val client = mock<ISftpClient>()
        doAnswer { invocation ->
            val onProgress = invocation.getArgument<(Long, Long) -> Unit>(2)
            onProgress(750, 2000)
        }.`when`(client).upload(any(), any(), any<(Long, Long) -> Unit>())

        val strategy = SftpTransferStrategy(client)
        val progress = strategy.upload("/local", "/remote", 2000, 0).first()

        assertEquals(750, progress.transferredBytes)
        assertEquals(2000, progress.totalBytes)
        verify(client).upload(any(), any(), any<(Long, Long) -> Unit>())
        verify(client, never()).uploadWithResume(any(), any(), any(), any<(Long, Long) -> Unit>())
    }

    @Test
    fun `upload with positive skip routes to resume upload`() = runTest {
        val client = mock<ISftpClient>()
        var capturedSkip = -1L
        doAnswer { invocation ->
            capturedSkip = invocation.getArgument(2)
            val onProgress = invocation.getArgument<(Long, Long) -> Unit>(3)
            onProgress(900, 2000)
        }.`when`(client).uploadWithResume(any(), any(), any(), any<(Long, Long) -> Unit>())

        val strategy = SftpTransferStrategy(client)
        val progress = strategy.upload("/local", "/remote", 2000, skip = 300).first()

        assertEquals(900, progress.transferredBytes)
        assertEquals(300, capturedSkip)
        verify(client, never()).upload(any(), any(), any<(Long, Long) -> Unit>())
    }

    @Test
    fun `download uses skip parameter from task offset`() = runTest {
        val client = mock<ISftpClient>()
        var capturedSkip = -1L
        doAnswer { invocation ->
            capturedSkip = invocation.getArgument(2)
            val onProgress = invocation.getArgument<(Long, Long) -> Unit>(3)
            onProgress(100, 500)
        }.`when`(client).downloadWithResume(
            any(), any(), any(), any<(Long, Long) -> Unit>()
        )

        val strategy = SftpTransferStrategy(client)
        strategy.download("/r", "/l", skip = 300).first()

        assertEquals(300, capturedSkip)
    }
}
