package com.example.sftping.transfer.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Re-run a failed upload from scratch (offset 0 → OVERWRITE) by re-enqueuing its worker. */
class RetryUseCase @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(id: Long) {
        val task = dao.get(id) ?: return
        // Only failed uploads can be retried: an upload's cache file survives failure
        // (the worker deletes it only on success), so the worker can re-read it.
        if (task.status != TransferTaskStatus.FAILED ||
            task.direction != TransferTaskDirection.UPLOAD
        ) {
            return
        }
        // Reset the offset so the upload restarts via OVERWRITE (not RESUME).
        dao.updateProgress(id, 0, TransferTaskStatus.RUNNING)
        val workRequest = OneTimeWorkRequestBuilder<com.example.sftping.work.SftpTransferWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("sftping_transfer_$id")
            .setInputData(workDataOf("task_id" to id))
            .build()
        try { WorkManager.getInstance(context).enqueue(workRequest) } catch (_: IllegalStateException) {}
    }
}
