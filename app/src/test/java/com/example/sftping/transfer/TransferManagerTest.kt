package com.example.sftping.transfer

import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TransferManagerTest {

    @Test
    fun `start creates task and adds to items`() = runTest {
        val dao = FakeDao()
        val manager = TransferManager(dao)
        manager.start("f.txt", "/f.txt", "", 1000, TransferDirection.DOWNLOAD) { _, _ -> }
        assertEquals(1, manager.items.value.size)
        assertEquals("f.txt", manager.items.value[0].fileName)
    }

    @Test
    fun `pause marks item as PAUSED`() = runTest {
        val dao = FakeDao()
        val manager = TransferManager(dao)
        manager.start("f.txt", "/f.txt", "", 1000, TransferDirection.DOWNLOAD) { _, _ -> }
        manager.pause(1L)
        assertEquals(TransferStatus.PAUSED, manager.items.value[0].status)
    }

    @Test
    fun `getTransferred returns saved offset from DAO`() = runTest {
        val dao = FakeDao()
        dao.insert(TransferTask(
            remotePath = "/f", localUri = "", fileName = "f.txt",
            totalBytes = 1000, transferredBytes = 500,
            direction = TransferTaskDirection.DOWNLOAD,
            status = TransferTaskStatus.PAUSED
        ))
        val manager = TransferManager(dao)
        assertEquals(500L, manager.getTransferred(1L))
    }
}

private class FakeDao : TransferTaskDao {
    private val tasks = mutableMapOf<Long, TransferTask>()
    private var nextId = 1L

    override suspend fun insert(task: TransferTask): Long {
        val id = nextId++
        tasks[id] = task.copy(id = id)
        return id
    }

    override suspend fun updateProgress(id: Long, bytes: Long, status: TransferTaskStatus) {
        tasks[id]?.let { tasks[id] = it.copy(transferredBytes = bytes) }
    }

    override suspend fun updateTotal(id: Long, total: Long) {
        tasks[id]?.let { tasks[id] = it.copy(totalBytes = total) }
    }

    override suspend fun updateStatus(id: Long, status: TransferTaskStatus) {
        tasks[id]?.let { tasks[id] = it.copy(status = status) }
    }

    override suspend fun all(): List<TransferTask> = tasks.values.toList()

    override suspend fun get(id: Long): TransferTask? = tasks[id]

    override suspend fun delete(id: Long) { tasks.remove(id) }
}
