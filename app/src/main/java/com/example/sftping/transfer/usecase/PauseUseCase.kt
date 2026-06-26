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

class PauseUseCase @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(id: Long) {
        dao.updateStatus(id, TransferTaskStatus.PAUSED)
        try { WorkManager.getInstance(context).cancelAllWorkByTag("sftping_transfer_$id") } catch (_: IllegalStateException) {}
    }
}
