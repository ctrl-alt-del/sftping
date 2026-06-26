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
import com.example.sftping.data.transfer.TransferTaskDao
import com.example.sftping.data.transfer.TransferTaskDirection
import com.example.sftping.data.transfer.TransferTaskStatus
import com.example.sftping.transfer.usecase.DownloadUseCase
import com.example.sftping.transfer.usecase.UploadUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SftpTransferWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: TransferTaskDao,
    private val downloadUseCase: DownloadUseCase,
    private val uploadUseCase: UploadUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("task_id", -1)
        if (taskId < 0) return Result.failure()

        val task = dao.get(taskId) ?: return Result.failure()
        setForeground(createForegroundInfo(taskId, task.fileName))

        val result = when (task.direction) {
            TransferTaskDirection.DOWNLOAD -> downloadUseCase.execute(taskId)
            TransferTaskDirection.UPLOAD -> uploadUseCase.execute(taskId)
        }

        cancelNotification(taskId)
        return when {
            result.isSuccess -> {
                if (task.direction == TransferTaskDirection.UPLOAD) {
                    java.io.File(task.localUri).delete()
                }
                Result.success()
            }
            result.exceptionOrNull() is com.example.sftping.sftp.SftpException -> {
                dao.updateStatus(taskId, TransferTaskStatus.FAILED)
                Result.failure()
            }
            else -> Result.retry()
        }
    }

    private fun createForegroundInfo(taskId: Long, fileName: String): ForegroundInfo {
        createChannel()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText("Transferring…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setProgress(100, 0, true)
            .build()
        return ForegroundInfo(
            taskId.toInt(),
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
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

    companion object {
        const val CHANNEL_ID = "sftping_transfers"
    }
}
