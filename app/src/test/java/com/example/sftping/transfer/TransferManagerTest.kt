package com.example.sftping.transfer

import android.content.Context
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class TransferManagerTest {

    @Test
    fun `items flow reflects DAO data`() = runTest {
        val dao = FakeDao()
        val context = mock<Context>()
        val manager = TransferManager(dao, context)
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
        val manager = TransferManager(dao, context)
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
