package com.example.sftping.transfer.usecase

import android.content.Context
import androidx.work.WorkManager
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CancelUseCase @Inject constructor(
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(id: Long) {
        dao.updateStatus(id, TransferTaskStatus.CANCELLED)
        WorkManager.getInstance(context).cancelAllWorkByTag("sftping_transfer_$id")
        dao.delete(id)
    }
}
