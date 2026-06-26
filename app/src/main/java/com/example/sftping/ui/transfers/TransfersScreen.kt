package com.example.sftping.ui.transfers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sftping.transfer.TransferDirection
import com.example.sftping.transfer.TransferItem
import com.example.sftping.transfer.TransferStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(viewModel: TransfersViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val active = items.filter { it.status in listOf(TransferStatus.RUNNING, TransferStatus.PAUSED) }
    val done = items.filter { it.status !in listOf(TransferStatus.RUNNING, TransferStatus.PAUSED) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var detailItem by remember { mutableStateOf<TransferItem?>(null) }
    val selectMode = selectedIds.isNotEmpty()

    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Inbox, contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text("No transfers yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            if (selectMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            selectedIds.forEach { viewModel.cancel(it) }
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Transfers") },
                    actions = {
                        if (done.isNotEmpty()) {
                            TextButton(onClick = { selectedIds = done.map { it.id }.toSet() }) {
                                Text("Select")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (active.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "ACTIVE",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                }
                items(active, key = { it.id }) { item ->
                    ActiveCard(item, onCancel = { viewModel.cancel(item.id) })
                }
            }
            if (done.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "COMPLETED",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                }
                items(done, key = { it.id }) { item ->
                    DoneCard(
                        item = item,
                        selected = item.id in selectedIds,
                        selectMode = selectMode,
                        onToggle = {
                            selectedIds = if (item.id in selectedIds) selectedIds - item.id
                            else selectedIds + item.id
                        },
                        onTap = { detailItem = it },
                        onDelete = { viewModel.cancel(item.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    detailItem?.let { item ->
        DetailDialog(item, onClose = { detailItem = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveCard(item: TransferItem, onCancel: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onCancel()
            false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Cancel, contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = directionIcon(item),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.fileName, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium, maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            statusText(item),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (item.totalBytes > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { item.transferredBytes.toFloat() / item.totalBytes.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${item.formattedTransferred} / ${item.formattedTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DoneCard(
    item: TransferItem,
    selected: Boolean,
    selectMode: Boolean,
    onToggle: () -> Unit,
    onTap: (TransferItem) -> Unit,
    onDelete: () -> Unit
) {
    if (selectMode) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onToggle, onLongClick = onToggle),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                else CardDefaults.elevatedCardColors().containerColor
            )
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                doneIcon(item)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.fileName, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        statusText(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Checkbox(checked = selected, onCheckedChange = { onToggle() })
            }
        }
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
                .combinedClickable(onClick = { onTap(item) }, onLongClick = onToggle),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors()
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                doneIcon(item)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.fileName, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        statusText(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailDialog(item: TransferItem, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(
                imageVector = if (item.status == TransferStatus.FAILED) Icons.Filled.Error
                    else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (item.status == TransferStatus.FAILED) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(item.fileName) },
        text = {
            Column {
                Text(
                    item.remotePath,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row {
                    Text("${item.formattedTotal}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        directionLabel(item),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    statusLabel(item),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (item.status) {
                        TransferStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        TransferStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (item.lastModified > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        dateTimeFormat.format(Date(item.lastModified)),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}

private fun statusText(item: TransferItem): String = when (item.status) {
    TransferStatus.RUNNING -> "${item.formattedTransferred} of ${item.formattedTotal}"
    TransferStatus.PAUSED -> "Paused · ${item.formattedTransferred} of ${item.formattedTotal}"
    TransferStatus.COMPLETED -> if (item.lastModified > 0) "Completed on ${dateTimeFormat.format(Date(item.lastModified))}"
        else "Completed"
    TransferStatus.FAILED -> if (item.lastModified > 0) "Failed on ${dateTimeFormat.format(Date(item.lastModified))}"
        else "Failed"
    TransferStatus.CANCELLED -> "Cancelled"
}

private fun statusLabel(item: TransferItem): String = when (item.status) {
    TransferStatus.COMPLETED -> "Completed"
    TransferStatus.FAILED -> "Failed"
    TransferStatus.CANCELLED -> "Cancelled"
    TransferStatus.RUNNING -> "Running"
    TransferStatus.PAUSED -> "Paused"
}

private fun directionLabel(item: TransferItem): String = when (item.direction) {
    TransferDirection.DOWNLOAD -> "Download"
    TransferDirection.UPLOAD -> "Upload"
}

@Composable
private fun doneIcon(item: TransferItem) {
    val icon = when {
        item.status == TransferStatus.FAILED -> Icons.Filled.Error
        item.status == TransferStatus.CANCELLED -> Icons.Filled.Close
        else -> Icons.Filled.CheckCircle
    }
    Box(
        Modifier.size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when (item.status) {
                    TransferStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    TransferStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceContainerHighest
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = when (item.status) {
                TransferStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                TransferStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}

private fun directionIcon(item: TransferItem) = when (item.direction) {
    TransferDirection.UPLOAD -> Icons.Filled.Upload
    TransferDirection.DOWNLOAD -> Icons.Filled.Download
}

private val TransferItem.formattedTransferred: String get() = formatBytes(transferredBytes)
private val TransferItem.formattedTotal: String get() = formatBytes(totalBytes)

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
    bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
}
