package com.example.sftping.transfer

import android.content.Context
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.transfer.usecase.CancelUseCase
import com.example.sftping.transfer.usecase.EnqueueUseCase
import com.example.sftping.transfer.usecase.PauseUseCase
import com.example.sftping.transfer.usecase.ResumeUseCase
import com.example.sftping.transfer.usecase.RetryUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.times

class TransferManagerTest {

    private val mockEnqueue = mock<EnqueueUseCase>()
    private val mockPause = mock<PauseUseCase>()
    private val mockResume = mock<ResumeUseCase>()
    private val mockCancel = mock<CancelUseCase>()
    private val mockRetry = mock<RetryUseCase>()

    @Test
    fun `items flow reflects DAO data`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel, mockRetry)
        dao.insert(
            TransferTask(
                remotePath = "a", fileName = "f.txt",
                totalBytes = 100, transferredBytes = 0,
                direction = TransferTaskDirection.DOWNLOAD,
                status = TransferTaskStatus.RUNNING
            )
        )
        assertEquals(1, manager.items.first { it.isNotEmpty() }.size)
    }

    @Test
    fun `getTransferred returns saved offset`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel, mockRetry)
        dao.insert(
            TransferTask(
                remotePath = "/f", fileName = "f.txt",
                totalBytes = 1000, transferredBytes = 500,
                direction = TransferTaskDirection.DOWNLOAD,
                status = TransferTaskStatus.PAUSED
            )
        )
        assertEquals(500L, manager.getTransferred(1L))
    }

    @Test
    fun `completedUploadPaths returns only completed upload remote paths`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel, mockRetry)
        dao.insert(
            TransferTask(
                remotePath = "/d/a.txt", fileName = "a.txt", totalBytes = 1, transferredBytes = 1,
                direction = TransferTaskDirection.UPLOAD, status = TransferTaskStatus.COMPLETED
            )
        )
        dao.insert(
            TransferTask(
                remotePath = "/d/b.txt", fileName = "b.txt", totalBytes = 1, transferredBytes = 0,
                direction = TransferTaskDirection.UPLOAD, status = TransferTaskStatus.RUNNING
            )
        )
        dao.insert(
            TransferTask(
                remotePath = "/d/c.txt", fileName = "c.txt", totalBytes = 1, transferredBytes = 1,
                direction = TransferTaskDirection.DOWNLOAD, status = TransferTaskStatus.COMPLETED
            )
        )

        assertEquals(setOf("/d/a.txt"), manager.completedUploadPaths())
    }

    @Test
    fun `retryAllFailed retries only FAILED uploads`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel, mockRetry)
        dao.insert(
            TransferTask(
                remotePath = "/a", fileName = "a.txt", totalBytes = 10, transferredBytes = 0,
                direction = TransferTaskDirection.UPLOAD, status = TransferTaskStatus.FAILED
            )
        )
        dao.insert(
            TransferTask(
                remotePath = "/b", fileName = "b.txt", totalBytes = 10, transferredBytes = 0,
                direction = TransferTaskDirection.UPLOAD, status = TransferTaskStatus.FAILED
            )
        )
        dao.insert(
            TransferTask(
                remotePath = "/c", fileName = "c.txt", totalBytes = 10, transferredBytes = 0,
                direction = TransferTaskDirection.DOWNLOAD, status = TransferTaskStatus.FAILED
            )
        )
        dao.insert(
            TransferTask(
                remotePath = "/d", fileName = "d.txt", totalBytes = 10, transferredBytes = 10,
                direction = TransferTaskDirection.UPLOAD, status = TransferTaskStatus.COMPLETED
            )
        )

        manager.retryAllFailed()

        verify(mockRetry, times(2)).execute(org.mockito.kotlin.any())
        verify(mockRetry, never()).execute(3L)
        verify(mockRetry, never()).execute(4L)
    }

    @Test
    fun `retryAllFailed is no-op when no failed uploads`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel, mockRetry)
        dao.insert(
            TransferTask(
                remotePath = "/a", fileName = "a.txt", totalBytes = 10, transferredBytes = 0,
                direction = TransferTaskDirection.DOWNLOAD, status = TransferTaskStatus.FAILED
            )
        )
        dao.insert(
            TransferTask(
                remotePath = "/b", fileName = "b.txt", totalBytes = 10, transferredBytes = 10,
                direction = TransferTaskDirection.UPLOAD, status = TransferTaskStatus.COMPLETED
            )
        )

        manager.retryAllFailed()

        verify(mockRetry, never()).execute(org.mockito.kotlin.any())
    }

    @Test
    fun `retryAllFailed is no-op with empty dao`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel, mockRetry)

        manager.retryAllFailed()

        verify(mockRetry, never()).execute(org.mockito.kotlin.any())
    }

}

private class FakeDao : TransferTaskDao {
    private val tasks = mutableMapOf<Long, TransferTask>()
    private var nextId = 1L
    private val _flow = MutableSharedFlow<List<TransferTask>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun insert(task: TransferTask): Long {
        val id = nextId++
        tasks[id] = task.copy(id = id)
        _flow.emit(tasks.values.toList())
        return id
    }

    override suspend fun updateProgress(id: Long, bytes: Long, status: TransferTaskStatus) {
        tasks[id]?.let { tasks[id] = it.copy(transferredBytes = bytes, status = status) }
        _flow.emit(tasks.values.toList())
    }

    override suspend fun updateTotal(id: Long, total: Long) {
        tasks[id]?.let { tasks[id] = it.copy(totalBytes = total) }
    }

    override suspend fun updateStatus(id: Long, status: TransferTaskStatus) {
        tasks[id]?.let { tasks[id] = it.copy(status = status) }
        _flow.emit(tasks.values.toList())
    }

    override suspend fun all(): List<TransferTask> = tasks.values.toList()

    override fun observeAll(): Flow<List<TransferTask>> = _flow

    override suspend fun get(id: Long): TransferTask? = tasks[id]

    override suspend fun delete(id: Long) { tasks.remove(id); _flow.emit(tasks.values.toList()) }
}
