package com.example.sftping.ui.files

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.RemoteFile
import com.example.sftping.sftp.SessionState
import com.example.sftping.sftp.SftpException
import com.example.sftping.transfer.TransferDirection
import com.example.sftping.transfer.TransferItem
import com.example.sftping.transfer.TransferManager
import com.example.sftping.transfer.TransferStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

data class FilesUiState(
    val currentPath: String = "/",
    val files: List<RemoteFile> = emptyList(),
    val rawFiles: List<RemoteFile> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val pathStack: List<String> = emptyList(),
    val multiSelectMode: Boolean = false,
    val selectedPaths: List<String> = emptyList(),
    val renamingFile: RemoteFile? = null,
    val showHidden: Boolean = false,
    val sortMode: SortMode = SortMode.NAME_ASC,
    val searchQuery: String = ""
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val sftpClient: ISftpClient,
    private val transferManager: TransferManager,
    @ApplicationContext private val context: Context,
    private val sessionState: SessionState
) : ViewModel() {

    var uiState by mutableStateOf(FilesUiState())
        private set

    private val _navigateToConnection = MutableSharedFlow<Unit>()
    val navigateToConnection: SharedFlow<Unit> = _navigateToConnection

    fun loadFiles(path: String = sessionState.initialDirectory) {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            try {
                val fetched = sftpClient.listFiles(path)
                uiState = uiState.copy(
                    currentPath = path,
                    rawFiles = fetched,
                    files = FileView.apply(fetched, uiState.showHidden, uiState.searchQuery, uiState.sortMode),
                    loading = false
                )
            } catch (e: SftpException) {
                uiState = uiState.copy(loading = false, error = e.message ?: "List failed")
            } catch (_: IllegalStateException) {
                uiState = uiState.copy(loading = false, error = "Not connected")
                viewModelScope.launch { _navigateToConnection.emit(Unit) }
            } catch (e: Exception) {
                uiState = uiState.copy(loading = false, error = e.message ?: "Error")
            }
        }
    }

    fun navigateTo(folder: String) {
        val newPath = if (uiState.currentPath == "/") "/$folder"
        else "${uiState.currentPath}/$folder"
        uiState = uiState.copy(pathStack = uiState.pathStack + uiState.currentPath, searchQuery = "")
        loadFiles(newPath)
    }

    fun navigateBack() {
        val stack = uiState.pathStack
        if (stack.isEmpty()) return
        val previous = stack.last()
        uiState = uiState.copy(pathStack = stack.dropLast(1), searchQuery = "")
        loadFiles(previous)
    }

    fun canGoBack() = uiState.pathStack.isNotEmpty()

    fun setSortMode(mode: SortMode) {
        uiState = uiState.copy(sortMode = mode)
        recompute()
    }

    fun toggleShowHidden() {
        uiState = uiState.copy(showHidden = !uiState.showHidden)
        recompute()
    }

    fun setSearchQuery(query: String) {
        uiState = uiState.copy(searchQuery = query)
        recompute()
    }

    private fun recompute() {
        uiState = uiState.copy(
            files = FileView.apply(uiState.rawFiles, uiState.showHidden, uiState.searchQuery, uiState.sortMode)
        )
    }

    fun clearSelection() {
        uiState = uiState.copy(multiSelectMode = false, selectedPaths = emptyList())
    }

    fun toggleSelection(path: String) {
        val selected = uiState.selectedPaths.toMutableList()
        if (path in selected) {
            selected.remove(path)
            if (selected.isEmpty()) {
                uiState = uiState.copy(multiSelectMode = false, selectedPaths = emptyList())
                return
            }
        } else {
            selected.add(path)
        }
        uiState = uiState.copy(multiSelectMode = selected.isNotEmpty(), selectedPaths = selected)
    }

    fun startRename(file: RemoteFile) {
        uiState = uiState.copy(renamingFile = file)
    }

    fun cancelRename() {
        uiState = uiState.copy(renamingFile = null)
    }

    fun renameFile(newName: String) {
        val file = uiState.renamingFile ?: return
        val newPath = if (uiState.currentPath == "/") "/$newName"
        else "${uiState.currentPath}/$newName"
        viewModelScope.launch {
            try {
                sftpClient.rename(file.path, newPath)
                uiState = uiState.copy(renamingFile = null)
                loadFiles(uiState.currentPath)
            } catch (e: SftpException) {
                uiState = uiState.copy(
                    error = e.message ?: "Rename failed",
                    renamingFile = null
                )
            }
        }
    }

    fun deleteSelected() {
        val paths = uiState.selectedPaths
        if (paths.isEmpty()) return
        viewModelScope.launch {
            uiState = uiState.copy(error = null)
            try {
                for (path in paths) {
                    sftpClient.delete(path)
                }
                uiState = uiState.copy(multiSelectMode = false, selectedPaths = emptyList())
                loadFiles(uiState.currentPath)
            } catch (e: SftpException) {
                uiState = uiState.copy(error = e.message ?: "Delete failed")
            }
        }
    }

    fun uploadFile(uri: Uri) {
        viewModelScope.launch {
            val fileName = resolveFileName(uri) ?: "uploaded_file"
            val remotePath = if (uiState.currentPath == "/") "/$fileName"
            else "${uiState.currentPath}/$fileName"
            val tempFile = copyUriToCache(uri, fileName) ?: return@launch

            val taskId = transferManager.enqueue(
                fileName, remotePath, tempFile.absolutePath, tempFile.length(),
                TransferDirection.UPLOAD
            )

            // Rename cache to match UploadUseCase naming: sftping_ul_<taskId>_<fileName>
            val named = File(context.cacheDir, "sftping_ul_${taskId}_$fileName")
            tempFile.renameTo(named)

            // Wait for Worker completion, then refresh
            viewModelScope.launch {
                transferManager.items.collect { items ->
                    val item = items.find { it.id == taskId }
                    if (item != null && item.status == TransferStatus.COMPLETED) {
                        loadFiles(uiState.currentPath)
                        return@collect
                    }
                }
            }
        }
    }

    fun downloadFile(remotePath: String, destUri: Uri) {
        viewModelScope.launch {
            val fileName = remotePath.substringAfterLast("/")
            val taskId = transferManager.enqueue(
                fileName, remotePath, destUri.toString(), 0,
                TransferDirection.DOWNLOAD
            )
            viewModelScope.launch {
                transferManager.items.collect { items ->
                    val item = items.find { it.id == taskId }
                    if (item != null && item.status == TransferStatus.COMPLETED) {
                        val cacheFile = File(context.cacheDir, "sftping_dl_${taskId}_${fileName}")
                        copyCacheToUri(cacheFile, destUri)
                        cacheFile.delete()
                        return@collect
                    }
                }
            }
        }
    }

    fun clearError() { uiState = uiState.copy(error = null) }

    private suspend fun copyUriToCache(uri: Uri, fileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(context.cacheDir, "sftping_ul_$fileName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
            }
            cacheFile
        } catch (e: SecurityException) {
            android.util.Log.w("FilesViewModel", "Stale SAF permission for $uri", e)
            uiState = uiState.copy(
                error = "Permission to this file has expired. Please re-select it."
            )
            null
        } catch (e: Exception) {
            android.util.Log.e("FilesViewModel", "Failed to copy URI to cache", e)
            uiState = uiState.copy(error = "Cannot read selected file")
            null
        }
    }

    private suspend fun copyCacheToUri(cacheFile: File, destUri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(cacheFile).use { input -> input.copyTo(output) }
            }
        } catch (e: SecurityException) {
            android.util.Log.w("FilesViewModel", "Stale SAF permission for destination $destUri", e)
            uiState = uiState.copy(
                error = "Cannot write to destination — permission may have expired."
            )
        } catch (e: Exception) {
            android.util.Log.e("FilesViewModel", "Failed to copy cache to URI", e)
        }
    }

    private suspend fun resolveFileName(uri: Uri): String? = withContext(Dispatchers.IO) {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex("_display_name")
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
        name ?: uri.lastPathSegment
    }
}
