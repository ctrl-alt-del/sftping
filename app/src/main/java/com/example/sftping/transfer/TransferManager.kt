package com.example.sftping.transfer

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.work.SftpTransferWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferManager @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val items: StateFlow<List<TransferItem>> = dao.observeAll()
        .map { list -> list.map { it.toTransferItem() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private var nextId = 0L

    suspend fun enqueue(
        fileName: String, remotePath: String, localUri: String, totalBytes: Long,
        direction: TransferDirection
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
        val workRequest = OneTimeWorkRequestBuilder<SftpTransferWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(workDataOf("task_id" to id))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
        return id
    }

    suspend fun pause(id: Long) {
        dao.updateStatus(id, TransferTaskStatus.PAUSED)
        WorkManager.getInstance(context).cancelAllWorkByTag("sftping_transfer_$id")
    }

    suspend fun resume(id: Long) {
        val task = dao.get(id) ?: return
        val workRequest = OneTimeWorkRequestBuilder<SftpTransferWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("sftping_transfer_$id")
            .setInputData(workDataOf("task_id" to id))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
        dao.updateStatus(id, TransferTaskStatus.RUNNING)
    }

    suspend fun cancel(id: Long) {
        dao.updateStatus(id, TransferTaskStatus.CANCELLED)
        WorkManager.getInstance(context).cancelAllWorkByTag("sftping_transfer_$id")
        dao.delete(id)
    }

    suspend fun getTransferred(id: Long): Long = dao.get(id)?.transferredBytes ?: 0L
}

private fun TransferTask.toTransferItem() = TransferItem(
    id = id, fileName = fileName, remotePath = remotePath,
    direction = when (direction) {
        TransferTaskDirection.DOWNLOAD -> TransferDirection.DOWNLOAD
        TransferTaskDirection.UPLOAD -> TransferDirection.UPLOAD
    },
    totalBytes = totalBytes, transferredBytes = transferredBytes,
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
