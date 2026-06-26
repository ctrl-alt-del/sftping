package com.example.sftping.transfer.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResumeUseCase @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(id: Long) {
        val task = dao.get(id) ?: return
        val workRequest = OneTimeWorkRequestBuilder<com.example.sftping.work.SftpTransferWorker>()
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
}
