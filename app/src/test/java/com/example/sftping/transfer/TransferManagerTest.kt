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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class TransferManagerTest {

    private val mockEnqueue = mock<EnqueueUseCase>()
    private val mockPause = mock<PauseUseCase>()
    private val mockResume = mock<ResumeUseCase>()
    private val mockCancel = mock<CancelUseCase>()

    @Test
    fun `items flow reflects DAO data`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel)
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
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel)
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
        val manager = TransferManager(dao, context, mockEnqueue, mockPause, mockResume, mockCancel)
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
