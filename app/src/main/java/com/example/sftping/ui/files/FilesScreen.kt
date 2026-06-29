package com.example.sftping.ui.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sftping.sftp.RemoteFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(
    onNavigateToConnection: () -> Unit,
    viewModel: FilesViewModel = viewModel()
) {
    val state = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var fabExpanded by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadFiles() }
    LaunchedEffect(Unit) { viewModel.navigateToConnection.collect { onNavigateToConnection() } }
    LaunchedEffect(state.error) { state.error?.let { snackbarHostState.showSnackbar(it) } }

    val uploadPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.uploadFile(it) }
    }

    var downloadTargetPath by remember { mutableStateOf<String?>(null) }
    val downloadPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val path = downloadTargetPath ?: return@rememberLauncherForActivityResult
        downloadTargetPath = null
        uri?.let { viewModel.downloadFile(path, it) }
    }

    Scaffold(
        topBar = {
            if (state.multiSelectMode) {
                TopAppBar(
                    title = { Text("${state.selectedPaths.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                downloadTargetPath = state.selectedPaths.firstOrNull()
                                val suggestedName = downloadTargetPath?.substringAfterLast("/") ?: "file"
                                downloadPicker.launch(suggestedName)
                            },
                            enabled = state.selectedPaths.size == 1
                        ) { Icon(Icons.Filled.Download, contentDescription = "Download") }
                        IconButton(
                            onClick = {
                                val file = state.files.find { it.path == state.selectedPaths.firstOrNull() }
                                if (file != null && state.selectedPaths.size == 1) viewModel.startRename(file)
                            },
                            enabled = state.selectedPaths.size == 1
                        ) { Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = "Rename") }
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                Column {
                    TopAppBar(
                        title = { Text(state.currentPath, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        navigationIcon = {
                            IconButton(onClick = viewModel::navigateBack, enabled = viewModel.canGoBack()) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                searchActive = !searchActive
                                if (!searchActive) viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            Box {
                                IconButton(onClick = { sortMenuExpanded = true }) {
                                    Icon(Icons.Filled.Sort, contentDescription = "Sort and filter")
                                }
                                SortFilterMenu(
                                    expanded = sortMenuExpanded,
                                    sortMode = state.sortMode,
                                    showHidden = state.showHidden,
                                    onDismiss = { sortMenuExpanded = false },
                                    onSortSelected = {
                                        viewModel.setSortMode(it)
                                        sortMenuExpanded = false
                                    },
                                    onToggleHidden = { viewModel.toggleShowHidden() }
                                )
                            }
                            IconButton(onClick = { viewModel.loadFiles(state.currentPath) }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                            }
                        }
                    )
                    if (searchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            placeholder = { Text("Search this folder") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                    searchActive = false
                                }) { Icon(Icons.Filled.Close, contentDescription = "Clear search") }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.multiSelectMode) {
                Column(horizontalAlignment = Alignment.End) {
                    if (fabExpanded) {
                        SmallFloatingActionButton(
                            onClick = { fabExpanded = false; uploadPicker.launch(arrayOf("*/*")) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Filled.Upload, contentDescription = "Upload file")
                        }
                        Spacer(Modifier.height(10.dp))
                        SmallFloatingActionButton(
                            onClick = { fabExpanded = false; viewModel.loadFiles(state.currentPath) },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Icon(Icons.Filled.CreateNewFolder, contentDescription = "New folder")
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    ExtendedFloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        icon = { Icon(if (fabExpanded) Icons.Filled.Close else Icons.Filled.Add, contentDescription = null) },
                        text = { Text(if (fabExpanded) "Close" else "Add") }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.loading && state.files.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.files.isEmpty()) {
                Text(
                    if (state.searchQuery.isNotBlank()) "No matching files" else "This folder is empty",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(state.files, key = { it.path }) { file ->
                        val selected = file.path in state.selectedPaths
                        FileRow(
                            file = file,
                            selected = selected,
                            multiSelect = state.multiSelectMode,
                            onClick = {
                                if (state.multiSelectMode) viewModel.toggleSelection(file.path)
                                else if (file.isDirectory) viewModel.navigateTo(file.name)
                            },
                            onLongClick = { viewModel.toggleSelection(file.path) }
                        )
                    }
                }
            }
        }
    }

    if (state.renamingFile != null) {
        var newName by remember(state.renamingFile) { mutableStateOf(state.renamingFile.name) }
        AlertDialog(
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = newName, onValueChange = { newName = it },
                    label = { Text("New name") },
                    singleLine = true
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.renameFile(newName) }) { Text("Rename") } },
            dismissButton = { TextButton(onClick = viewModel::cancelRename) { Text("Cancel") } },
            onDismissRequest = viewModel::cancelRename
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    file: RemoteFile,
    selected: Boolean,
    multiSelect: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = fileTypeIcon(file),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal)
            Text(fileSubtitle(file), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (multiSelect) {
            Spacer(Modifier.width(8.dp))
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun fileTypeIcon(file: RemoteFile): ImageVector = when {
    file.isDirectory -> Icons.Filled.Folder
    file.name.endsWith(".mp4") || file.name.endsWith(".mkv") || file.name.endsWith(".avi") -> Icons.Filled.Movie
    file.name.endsWith(".jpg") || file.name.endsWith(".png") || file.name.endsWith(".gif") -> Icons.Filled.Image
    file.name.endsWith(".tar.gz") || file.name.endsWith(".zip") || file.name.endsWith(".7z") -> Icons.Filled.SwapVert
    file.name.endsWith(".sh") || file.name.endsWith(".json") || file.name.endsWith(".xml") -> Icons.Filled.Description
    else -> Icons.Filled.InsertDriveFile
}

private val dateFormat = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())

private fun fileSubtitle(file: RemoteFile): String {
    val size = file.formattedSize
    val date = dateFormat.format(Date(file.lastModified * 1000L))
    return if (file.isDirectory) date else "$size · $date"
}

private val sortOptions = listOf(
    SortMode.NAME_ASC to "Name (A–Z)",
    SortMode.NAME_DESC to "Name (Z–A)",
    SortMode.SIZE to "Size",
    SortMode.LAST_MODIFIED to "Last modified"
)

@Composable
private fun SortFilterMenu(
    expanded: Boolean,
    sortMode: SortMode,
    showHidden: Boolean,
    onDismiss: () -> Unit,
    onSortSelected: (SortMode) -> Unit,
    onToggleHidden: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Text(
            "Sort by",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        sortOptions.forEach { (mode, label) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = { onSortSelected(mode) },
                trailingIcon = {
                    if (mode == sortMode) {
                        Icon(Icons.Filled.Check, contentDescription = "Selected")
                    }
                }
            )
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Show hidden files") },
            onClick = onToggleHidden,
            trailingIcon = {
                if (showHidden) {
                    Icon(Icons.Filled.Check, contentDescription = "Enabled")
                }
            }
        )
    }
}
