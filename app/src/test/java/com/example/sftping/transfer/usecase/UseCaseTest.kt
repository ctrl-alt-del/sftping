package com.example.sftping.transfer.usecase

import android.content.Context
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.transfer.strategy.TransferProgress
import com.example.sftping.transfer.strategy.TransferStrategy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File
import java.io.RandomAccessFile

class DownloadUseCaseTest {

    @Test
    fun `successful download updates status to COMPLETED`() = runTest {
        val dao = FakeDao()
        val strategy = mock<TransferStrategy>()
        val context = mock<Context>()
        doReturn(File(System.getProperty("java.io.tmpdir"))).`when`(context).cacheDir
        doReturn(flowOf(TransferProgress(1000, 1000))).`when`(strategy)
            .download(any(), any(), any())

        val taskId = dao.insert(makeTask(1000, 0, TransferTaskDirection.DOWNLOAD, TransferTaskStatus.RUNNING))
        val useCase = DownloadUseCase(strategy, dao, context)
        val result = useCase.execute(taskId)

        assertTrue(result.isSuccess)
        assertEquals(TransferTaskStatus.COMPLETED, dao.get(taskId)?.status)
    }
}

class UploadUseCaseTest {

    @Test
    fun `upload completes successfully`() = runTest {
        val dao = FakeDao()
        val strategy = mock<TransferStrategy>()
        val context = mock<Context>()
        doReturn(File(System.getProperty("java.io.tmpdir"))).`when`(context).cacheDir
        doReturn(flowOf(TransferProgress(800, 800))).`when`(strategy)
            .upload(any(), any(), any(), any())

        val taskId = dao.insert(makeTask(800, 400, TransferTaskDirection.UPLOAD, TransferTaskStatus.PAUSED))

        val cacheFile = File(System.getProperty("java.io.tmpdir"), "sftping_ul_${taskId}_f.bin")
        RandomAccessFile(cacheFile, "rw").setLength(800)
        cacheFile.deleteOnExit()

        val useCase = UploadUseCase(strategy, dao, context)
        val result = useCase.execute(taskId)

        assertTrue(result.isSuccess)
    }
}

private fun makeTask(
    total: Long, transferred: Long, direction: TransferTaskDirection, status: TransferTaskStatus
) = TransferTask(
    remotePath = "/r", fileName = "f.bin",
    totalBytes = total, transferredBytes = transferred,
    direction = direction, status = status
)

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
