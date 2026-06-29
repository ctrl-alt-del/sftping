package com.example.sftping.transfer

import android.content.Context
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.transfer.usecase.CancelUseCase
import com.example.sftping.transfer.usecase.EnqueueUseCase
import com.example.sftping.transfer.usecase.PauseUseCase
import com.example.sftping.transfer.usecase.ResumeUseCase
import com.example.sftping.transfer.usecase.RetryUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferManager @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context,
    private val enqueueUseCase: EnqueueUseCase,
    private val pauseUseCase: PauseUseCase,
    private val resumeUseCase: ResumeUseCase,
    private val cancelUseCase: CancelUseCase,
    private val retryUseCase: RetryUseCase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val items: StateFlow<List<TransferItem>> = dao.observeAll()
        .map { list -> list.map { it.toTransferItem() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    suspend fun enqueue(
        fileName: String, remotePath: String, localUri: String, totalBytes: Long,
        direction: TransferDirection
    ): Long {
        return enqueueUseCase.execute(fileName, remotePath, localUri, totalBytes, direction)
    }

    suspend fun pause(id: Long) = pauseUseCase.execute(id)

    suspend fun resume(id: Long) = resumeUseCase.execute(id)

    suspend fun cancel(id: Long) = cancelUseCase.execute(id)

    suspend fun retry(id: Long) = retryUseCase.execute(id)

    suspend fun getTransferred(id: Long): Long = dao.get(id)?.transferredBytes ?: 0L

    /** Remote paths of uploads that have completed — used to flag already-uploaded files. */
    suspend fun completedUploadPaths(): Set<String> =
        dao.all()
            .filter {
                it.direction == com.example.sftping.data.transfer.TransferTaskDirection.UPLOAD &&
                    it.status == com.example.sftping.data.transfer.TransferTaskStatus.COMPLETED
            }
            .map { it.remotePath }
            .toSet()
}

private fun com.example.sftping.data.transfer.TransferTask.toTransferItem() = TransferItem(
    id = id, fileName = fileName, remotePath = remotePath,
    direction = when (direction) {
        com.example.sftping.data.transfer.TransferTaskDirection.DOWNLOAD -> TransferDirection.DOWNLOAD
        com.example.sftping.data.transfer.TransferTaskDirection.UPLOAD -> TransferDirection.UPLOAD
    },
    totalBytes = totalBytes, transferredBytes = transferredBytes,
    lastModified = lastModified,
    status = when (status) {
        com.example.sftping.data.transfer.TransferTaskStatus.RUNNING -> TransferStatus.RUNNING
        com.example.sftping.data.transfer.TransferTaskStatus.COMPLETED -> TransferStatus.COMPLETED
        com.example.sftping.data.transfer.TransferTaskStatus.FAILED -> TransferStatus.FAILED
        com.example.sftping.data.transfer.TransferTaskStatus.PAUSED -> TransferStatus.PAUSED
        com.example.sftping.data.transfer.TransferTaskStatus.PENDING -> TransferStatus.RUNNING
        com.example.sftping.data.transfer.TransferTaskStatus.CANCELLED -> TransferStatus.CANCELLED
    }
)
