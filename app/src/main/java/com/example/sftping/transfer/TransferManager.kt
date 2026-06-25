package com.example.sftping.transfer

import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferManager @Inject constructor(
    private val dao: TransferTaskDao
) {
    private val _items = MutableStateFlow<List<TransferItem>>(emptyList())
    val items: StateFlow<List<TransferItem>> = _items

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch { _items.value = dao.all().map { it.toTransferItem() } }
    }

    suspend fun start(
        fileName: String, remotePath: String, localUri: String, totalBytes: Long,
        direction: TransferDirection, block: suspend (id: Long, progress: (Long, Long) -> Unit) -> Unit
    ): Long {
        val task = TransferTask(
            remotePath = remotePath,
            localUri = localUri,
            fileName = fileName,
            totalBytes = totalBytes,
            transferredBytes = 0,
            direction = direction.toTaskDirection(),
            status = TransferTaskStatus.RUNNING
        )
        val id = dao.insert(task)
        add(TransferItem(id, fileName, remotePath, direction, totalBytes, 0, TransferStatus.RUNNING))
        scope.launch {
            try {
                block(id) { transferred, total ->
                    updateProgress(id, transferred, total)
                    if (transferred % (1024 * 1024) < 64 * 1024) {
                        scope.launch {
                            dao.updateProgress(id, transferred, TransferTaskStatus.RUNNING)
                            if (total > 0) dao.updateTotal(id, total)
                        }
                    }
                }
                mark(id, TransferStatus.COMPLETED)
                dao.updateStatus(id, TransferTaskStatus.COMPLETED)
            } catch (_: Exception) {
                mark(id, TransferStatus.FAILED)
                dao.updateStatus(id, TransferTaskStatus.FAILED)
            }
        }
        return id
    }

    fun pause(id: Long) {
        mark(id, TransferStatus.PAUSED)
        scope.launch { dao.updateStatus(id, TransferTaskStatus.PAUSED) }
    }

    suspend fun getTransferred(id: Long): Long =
        dao.get(id)?.transferredBytes ?: 0L

    fun cancel(id: Long) {
        mark(id, TransferStatus.CANCELLED)
        scope.launch { dao.delete(id) }
    }

    private fun add(item: TransferItem) { _items.update { it + item } }

    private fun updateProgress(id: Long, transferred: Long, total: Long) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(transferredBytes = transferred, totalBytes = total) else it }
        }
    }

    private fun mark(id: Long, status: TransferStatus) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(status = status) else it }
        }
    }
}

private fun TransferTask.toTransferItem() = TransferItem(
    id = id,
    fileName = fileName,
    remotePath = remotePath,
    direction = when (direction) {
        TransferTaskDirection.DOWNLOAD -> TransferDirection.DOWNLOAD
        TransferTaskDirection.UPLOAD -> TransferDirection.UPLOAD
    },
    totalBytes = totalBytes,
    transferredBytes = transferredBytes,
    status = when (status) {
        TransferTaskStatus.RUNNING -> TransferStatus.RUNNING
        TransferTaskStatus.COMPLETED -> TransferStatus.COMPLETED
        TransferTaskStatus.FAILED -> TransferStatus.FAILED
        TransferTaskStatus.PAUSED -> TransferStatus.PAUSED
        TransferTaskStatus.PENDING -> TransferStatus.RUNNING
        TransferTaskStatus.CANCELLED -> TransferStatus.CANCELLED
    }
)

private fun TransferDirection.toTaskDirection() = when (this) {
    TransferDirection.DOWNLOAD -> TransferTaskDirection.DOWNLOAD
    TransferDirection.UPLOAD -> TransferTaskDirection.UPLOAD
}
