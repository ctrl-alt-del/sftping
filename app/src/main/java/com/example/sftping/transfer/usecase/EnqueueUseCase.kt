package com.example.sftping.transfer.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.transfer.TransferDirection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EnqueueUseCase @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(
        fileName: String, 
        remotePath: String, 
        localUri: String, 
        totalBytes: Long,
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
        try {
            val workRequest = OneTimeWorkRequestBuilder<com.example.sftping.work.SftpTransferWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workDataOf("task_id" to id))
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (_: IllegalStateException) {}
        return id
    }
}
