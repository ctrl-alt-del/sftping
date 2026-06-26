package com.example.sftping.transfer.strategy

import com.example.sftping.sftp.ISftpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

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
    fun `upload emits TransferProgress from ISftpClient callback`() = runTest {
        val client = mock<ISftpClient>()
        doAnswer { invocation ->
            val onProgress = invocation.getArgument<(Long, Long) -> Unit>(3)
            onProgress(750, 2000)
        }.`when`(client).uploadWithResume(
            any(), any(), any(), any<(Long, Long) -> Unit>()
        )

        val strategy = SftpTransferStrategy(client)
        val progress = strategy.upload("/local", "/remote", 2000, 0).first()

        assertEquals(750, progress.transferredBytes)
        assertEquals(2000, progress.totalBytes)
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
