package com.example.sftping.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sftping.data.editor.EditorLocation
import com.example.sftping.ui.components.ConnectionIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel = viewModel()) {
    val state = viewModel.uiState

    // Fallback consume for a path handed over from the Files tab; the reactive
    // init collect usually handles it first, this covers first-time VM creation
    // ordering. Idempotent via consume-and-clear.
    LaunchedEffect(Unit) { viewModel.consumePendingEditIfAny() }

    if (state.showAddSheet) {
        LocationDialog(
            existing = state.editingLocation,
            onDismiss = viewModel::closeAddSheet,
            onConfirm = { id, label, path ->
                if (id == null) viewModel.addLocation(label, path)
                else viewModel.saveEditedLocation(id, label, path)
            }
        )
    }

    if (state.openLocation == null) {
        LocationsList(
            locations = state.locations,
            pendingPaths = state.pendingPaths,
            connected = state.connected,
            onOpen = viewModel::open,
            onAdd = viewModel::openAddSheet,
            onEdit = viewModel::startEditLocation,
            onDelete = viewModel::deleteLocation
        )
    } else {
        EditorPane(
            state = state,
            onBack = viewModel::close,
            onContentChange = viewModel::onContentChange,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onSave = viewModel::saveNow
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsList(
    locations: List<EditorLocation>,
    pendingPaths: Set<String>,
    connected: Boolean,
    onOpen: (EditorLocation) -> Unit,
    onAdd: () -> Unit,
    onEdit: (EditorLocation) -> Unit,
    onDelete: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                ConnectionIndicator(isConnected = connected)
                TopAppBar(title = {
                Column {
                    Text("Editor")
                    Text(
                        "Saved remote files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            })
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add location") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onAdd
            )
        }
    ) { padding ->
        if (locations.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Description, contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "No saved locations yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Add a remote file path to start editing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(locations, key = { it.id }) { loc ->
                    LocationRow(
                        location = loc,
                        pending = loc.remotePath in pendingPaths,
                        onOpen = { onOpen(loc) },
                        onEdit = { onEdit(loc) },
                        onDelete = { onDelete(loc.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LocationRow(
    location: EditorLocation,
    pending: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Description, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (pending) {
                    Box(
                        Modifier.size(8.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    location.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (pending) "Pending sync · ${location.remotePath}" else location.remotePath,
                style = MaterialTheme.typography.bodySmall,
                color = if (pending) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = { menuOpen = false; onEdit() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    onClick = { menuOpen = false; onDelete() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorPane(
    state: EditorUiState,
    onBack: () -> Unit,
    onContentChange: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit
) {
    val loc = state.openLocation ?: return
    Scaffold(
        topBar = {
            Column {
                ConnectionIndicator(isConnected = state.connected)
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(loc.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            loc.remotePath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUndo, enabled = state.editable && state.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = onRedo, enabled = state.editable && state.canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = onSave, enabled = state.editable) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
            }
        },
        bottomBar = { StatusBar(state) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                OutlinedTextField(
                    value = state.content,
                    onValueChange = onContentChange,
                    readOnly = !state.editable,
                    enabled = state.editable,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}

@Composable
private fun StatusBar(state: EditorUiState) {
    val (icon, text, color) = when (val s = state.saveStatus) {
        is SaveStatus.Saved -> Triple(
            Icons.Filled.Save,
            "Saved to server · ${timeFormat.format(Date(s.at))}",
            MaterialTheme.colorScheme.primary
        )
        SaveStatus.Saving -> Triple(Icons.Filled.Save, "Saving…", MaterialTheme.colorScheme.primary)
        SaveStatus.PendingSync -> Triple(
            Icons.Filled.CloudOff,
            "Cached · pending sync (offline)",
            MaterialTheme.colorScheme.tertiary
        )
        SaveStatus.NotConnected -> Triple(
            Icons.Filled.CloudOff,
            "Not connected — connect to edit",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        is SaveStatus.Error -> Triple(
            Icons.Filled.CloudOff, s.message, MaterialTheme.colorScheme.error
        )
        SaveStatus.Idle -> Triple(
            Icons.Filled.Description,
            if (state.connected) "Ready" else "Not connected — connect to edit",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Composable
private fun LocationDialog(
    existing: EditorLocation?,
    onDismiss: () -> Unit,
    onConfirm: (id: String?, label: String, path: String) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var path by remember { mutableStateOf(existing?.remotePath ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add location" else "Edit location") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Remote path") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(existing?.id, label, path) },
                enabled = path.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
