package com.example.sftping.transfer.usecase

import android.content.Context
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.transfer.strategy.TransferStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
    private val strategy: TransferStrategy,
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(taskId: Long): Result<Unit> {
        val task = dao.get(taskId) ?: return Result.failure(Exception("Task $taskId not found"))

        val cacheFile = File(context.cacheDir, "sftping_dl_${task.id}_${task.fileName}")

        return try {
            strategy.download(
                remotePath = task.remotePath,
                localPath = cacheFile.absolutePath,
                skip = task.transferredBytes
            ).collect { progress ->
                dao.updateProgress(taskId, progress.transferredBytes, TransferTaskStatus.RUNNING)
                if (progress.totalBytes > 0) {
                    dao.updateTotal(taskId, progress.totalBytes)
                }
            }
            dao.updateStatus(taskId, TransferTaskStatus.COMPLETED)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DownloadUseCase", "Download failed for task $taskId", e)
            dao.updateStatus(taskId, TransferTaskStatus.FAILED)
            Result.failure(e)
        }
    }
}
