package com.example.sftping.transfer.usecase

import android.content.Context
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class PauseUseCaseTest {

    @Test
    fun `execute updates status to PAUSED and cancels work`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        val useCase = PauseUseCase(dao, context)

        useCase.execute(42L)

        verify(dao).updateStatus(42L, TransferTaskStatus.PAUSED)
    }
}

class CancelUseCaseTest {

    @Test
    fun `execute updates status to CANCELLED and deletes from DAO`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        val useCase = CancelUseCase(dao, context)

        useCase.execute(42L)

        verify(dao).updateStatus(42L, TransferTaskStatus.CANCELLED)
        verify(dao).delete(42L)
    }
}

class ResumeUseCaseTest {

    @Test
    fun `execute reloads task and updates status to RUNNING`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        val task = TransferTask(
            remotePath = "/r", fileName = "f.txt",
            totalBytes = 100, transferredBytes = 0,
            direction = TransferTaskDirection.DOWNLOAD,
            status = TransferTaskStatus.PAUSED
        )
        doReturn(task).`when`(dao).get(42L)
        val useCase = ResumeUseCase(dao, context)

        useCase.execute(42L)

        verify(dao).get(42L)
        verify(dao).updateStatus(42L, TransferTaskStatus.RUNNING)
    }

    @Test
    fun `execute no-ops when task not found`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        doReturn(null).`when`(dao).get(42L)
        val useCase = ResumeUseCase(dao, context)

        useCase.execute(42L)

        verify(dao).get(42L)
        // updateStatus should NOT be called when task is null
    }
}

class RetryUseCaseTest {

    private fun task(status: TransferTaskStatus, direction: TransferTaskDirection) = TransferTask(
        remotePath = "/r/f.bin", fileName = "f.bin",
        totalBytes = 100, transferredBytes = 40,
        direction = direction, status = status
    )

    @Test
    fun `retries a failed upload by resetting offset and status to RUNNING`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        doReturn(task(TransferTaskStatus.FAILED, TransferTaskDirection.UPLOAD)).`when`(dao).get(7L)
        val useCase = RetryUseCase(dao, context)

        useCase.execute(7L)

        verify(dao).updateProgress(7L, 0, TransferTaskStatus.RUNNING)
    }

    @Test
    fun `no-ops for a non-failed task`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        doReturn(task(TransferTaskStatus.COMPLETED, TransferTaskDirection.UPLOAD)).`when`(dao).get(7L)
        val useCase = RetryUseCase(dao, context)

        useCase.execute(7L)

        verify(dao, never()).updateProgress(any(), any(), any())
    }

    @Test
    fun `no-ops for a failed download`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        doReturn(task(TransferTaskStatus.FAILED, TransferTaskDirection.DOWNLOAD)).`when`(dao).get(7L)
        val useCase = RetryUseCase(dao, context)

        useCase.execute(7L)

        verify(dao, never()).updateProgress(any(), any(), any())
    }

    @Test
    fun `no-ops when task not found`() = runTest {
        val dao = mock<TransferTaskDao>()
        val context = mock<Context>()
        doReturn(null).`when`(dao).get(7L)
        val useCase = RetryUseCase(dao, context)

        useCase.execute(7L)

        verify(dao, never()).updateProgress(any(), any(), any())
    }
}
