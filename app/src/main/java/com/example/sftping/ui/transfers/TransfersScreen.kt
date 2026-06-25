package com.example.sftping.ui.transfers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sftping.transfer.TransferDirection
import com.example.sftping.transfer.TransferItem
import com.example.sftping.transfer.TransferStatus

@Composable
fun TransfersScreen(viewModel: TransfersViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val active = items.filter { it.status in listOf(TransferStatus.RUNNING, TransferStatus.PAUSED) }
    val done = items.filter { it.status !in listOf(TransferStatus.RUNNING, TransferStatus.PAUSED) }

    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transfers yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (active.isNotEmpty()) {
            item {
                Text("ACTIVE", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
            }
            items(active, key = { it.id }) { item -> TransferCard(item) }
        }
        if (done.isNotEmpty()) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("COMPLETED", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
            }
            items(done, key = { it.id }) { item -> TransferCard(item) }
        }
    }
}

@Composable
private fun TransferCard(item: TransferItem) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when (item.status) {
                TransferStatus.COMPLETED -> Icons.Filled.Check
                TransferStatus.FAILED -> Icons.Filled.Error
                TransferStatus.PAUSED -> Icons.Filled.Pause
                else -> if (item.direction == TransferDirection.UPLOAD) Icons.Filled.Upload
                        else Icons.Filled.Download
            }
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp),
                tint = when (item.status) {
                    TransferStatus.FAILED -> MaterialTheme.colorScheme.error
                    TransferStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                })
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.fileName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(statusText(item), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (item.status in listOf(TransferStatus.RUNNING, TransferStatus.PAUSED) && item.totalBytes > 0) {
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { item.transferredBytes.toFloat() / item.totalBytes.toFloat() },
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${item.formattedTransferred} / ${item.formattedTotal} · ${item.formattedSpeed}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun statusText(item: TransferItem): String = when (item.status) {
    TransferStatus.RUNNING -> "${item.formattedTransferred} of ${item.formattedTotal}"
    TransferStatus.PAUSED -> "Paused · ${item.formattedTransferred} of ${item.formattedTotal}"
    TransferStatus.COMPLETED -> "Completed"
    TransferStatus.FAILED -> "Failed"
    TransferStatus.CANCELLED -> "Cancelled"
}

private val TransferItem.formattedTransferred: String get() = formatBytes(transferredBytes)
private val TransferItem.formattedTotal: String get() = formatBytes(totalBytes)
private val TransferItem.formattedSpeed: String get() = if (speed > 0) "${formatBytes(speed)}/s" else ""

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
    bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
}
