package com.example.sftping.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.sftping.MainActivity
import com.example.sftping.R
import com.example.sftping.data.transfer.TransferTask
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.SftpException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class SftpTransferWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sftpClient: ISftpClient,
    private val dao: TransferTaskDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("task_id", -1)
        if (taskId < 0) return Result.failure()

        val task = dao.get(taskId) ?: return Result.failure()
        setForeground(createForegroundInfo(taskId, task.fileName))

        return try {
            when (task.direction) {
                TransferTaskDirection.DOWNLOAD -> executeDownload(task)
                TransferTaskDirection.UPLOAD -> executeUpload(task)
            }
            dao.updateStatus(taskId, TransferTaskStatus.COMPLETED)
            cancelNotification(taskId)
            Result.success()
        } catch (e: SftpException) {
            dao.updateStatus(taskId, TransferTaskStatus.FAILED)
            cancelNotification(taskId)
            Result.failure()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun executeDownload(task: TransferTask) {
        val cacheFile = File(applicationContext.cacheDir, "sftping_wk_${task.id}_${task.fileName}")
        sftpClient.downloadWithResume(
            task.remotePath, cacheFile.absolutePath, task.transferredBytes
        ) { transferred, total ->
            updateNotification(task.id, task.fileName, transferred, total)
        }
        updateNotification(task.id, task.fileName, task.totalBytes, task.totalBytes)
    }

    private suspend fun executeUpload(task: TransferTask) {
        val localFile = File(applicationContext.cacheDir, "sftping_wk_${task.id}_${task.fileName}")
        if (!localFile.exists() || localFile.length() < task.transferredBytes) {
            throw SftpException("Local file for upload not available")
        }
        sftpClient.uploadWithResume(
            localFile.absolutePath, task.remotePath, task.transferredBytes
        ) { transferred, total ->
            updateNotification(task.id, task.fileName, transferred, total)
        }
        updateNotification(task.id, task.fileName, task.totalBytes, task.totalBytes)
    }

    private fun createForegroundInfo(taskId: Long, fileName: String): ForegroundInfo {
        createChannel()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText("Starting…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setProgress(100, 0, true)
            .build()
        return ForegroundInfo(taskId.toInt(), notification)
    }

    private fun updateNotification(taskId: Long, fileName: String, transferred: Long, total: Long) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText(
                if (total > 0) "${formatBytes(transferred)} / ${formatBytes(total)}"
                else formatBytes(transferred)
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .apply {
                if (total > 0) setProgress(total.toInt(), transferred.toInt(), false)
            }
            .build()
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(taskId.toInt(), notification)
    }

    private fun cancelNotification(taskId: Long) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(taskId.toInt())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "File transfers", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Active SFTP file transfers" }
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }

    companion object {
        const val CHANNEL_ID = "sftping_transfers"
    }
}
