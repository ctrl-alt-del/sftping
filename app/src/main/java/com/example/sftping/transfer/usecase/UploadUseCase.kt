package com.example.sftping.transfer.usecase

import android.content.Context
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.transfer.strategy.TransferStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.inject.Inject

class UploadUseCase @Inject constructor(
    private val strategy: TransferStrategy,
    private val dao: TransferTaskDao,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(taskId: Long): Result<Unit> {
        val task = dao.get(taskId) ?: return Result.failure(Exception("Task $taskId not found"))

        val localFile = File(context.cacheDir, "sftping_ul_${task.id}_${task.fileName}")
        if (!localFile.exists() || localFile.length() < task.transferredBytes) {
            return Result.failure(Exception("Local file for upload not available"))
        }

        return try {
            strategy.upload(
                localPath = localFile.absolutePath,
                remotePath = task.remotePath,
                totalBytes = task.totalBytes,
                skip = task.transferredBytes
            ).collect { progress ->
                dao.updateProgress(taskId, progress.transferredBytes, TransferTaskStatus.RUNNING)
            }
            dao.updateStatus(taskId, TransferTaskStatus.COMPLETED)
            Result.success(Unit)
        } catch (e: Exception) {
            dao.updateStatus(taskId, TransferTaskStatus.FAILED)
            Result.failure(e)
        }
    }
}
