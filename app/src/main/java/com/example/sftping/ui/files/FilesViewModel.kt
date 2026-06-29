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
import kotlinx.coroutines.flow.first
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
    val searchQuery: String = "",
    val uploadCandidates: List<UploadCandidate> = emptyList(),
    val showUploadSheet: Boolean = false
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

    private var loadedEpoch: Int = -1

    fun onEnterScreen() {
        if (sessionState.epoch != loadedEpoch) {
            // New connection: start at the host's home/default directory.
            loadedEpoch = sessionState.epoch
            uiState = uiState.copy(pathStack = emptyList(), searchQuery = "")
            loadFiles(sessionState.initialDirectory)
        } else {
            // Returning to the tab in the same session: reopen the last visited path.
            loadFiles(uiState.currentPath)
        }
    }

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
            val taskId = enqueueUpload(uri, fileName) ?: return@launch
            observeBatchThenRefresh(listOf(taskId))
        }
    }

    /** Resolve picked files and show the upload-selection sheet, flagging already-uploaded ones. */
    fun prepareUpload(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val uploaded = transferManager.completedUploadPaths()
            val candidates = uris.map { uri ->
                val (name, size) = resolveFileMeta(uri)
                UploadCandidate(
                    uri = uri,
                    name = name,
                    size = size,
                    alreadyUploaded = uploadRemotePath(uiState.currentPath, name) in uploaded
                )
            }
            uiState = uiState.copy(uploadCandidates = candidates, showUploadSheet = true)
        }
    }

    fun toggleUploadCandidate(uri: Uri) {
        uiState = uiState.copy(
            uploadCandidates = uiState.uploadCandidates.map {
                if (it.uri == uri) it.copy(selected = !it.selected) else it
            }
        )
    }

    fun cancelUpload() {
        uiState = uiState.copy(showUploadSheet = false, uploadCandidates = emptyList())
    }

    fun confirmUpload() {
        val selected = uiState.uploadCandidates.filter { it.selected }
        uiState = uiState.copy(showUploadSheet = false, uploadCandidates = emptyList())
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val ids = mutableListOf<Long>()
            for (candidate in selected) {
                enqueueUpload(candidate.uri, candidate.name)?.let { ids.add(it) }
            }
            if (ids.isNotEmpty()) observeBatchThenRefresh(ids)
        }
    }

    private suspend fun enqueueUpload(uri: Uri, fileName: String): Long? {
        val tempFile = copyUriToCache(uri, fileName) ?: return null
        val remotePath = uploadRemotePath(uiState.currentPath, fileName)
        val taskId = transferManager.enqueue(
            fileName, remotePath, tempFile.absolutePath, tempFile.length(),
            TransferDirection.UPLOAD
        )
        // Rename cache to match UploadUseCase naming: sftping_ul_<taskId>_<fileName>
        val named = File(context.cacheDir, "sftping_ul_${taskId}_$fileName")
        tempFile.renameTo(named)
        return taskId
    }

    /** Refresh the listing once every task in [ids] reaches a terminal state. */
    private fun observeBatchThenRefresh(ids: List<Long>) {
        val pending = ids.toSet()
        viewModelScope.launch {
            transferManager.items.first { items ->
                pending.all { id ->
                    when (items.find { it.id == id }?.status) {
                        TransferStatus.COMPLETED, TransferStatus.FAILED, TransferStatus.CANCELLED -> true
                        else -> false
                    }
                }
            }
            loadFiles(uiState.currentPath)
        }
    }

    fun downloadFile(remotePath: String, destUri: Uri) {
        viewModelScope.launch {
            val fileName = remotePath.substringAfterLast("/")
            val taskId = enqueueDownload(remotePath, fileName, destUri)
            observeDownloadThenCopy(taskId, fileName, destUri)
        }
    }

    /** Download every selected non-directory file into the chosen [treeUri] folder. */
    fun downloadFiles(remotePaths: List<String>, treeUri: Uri) {
        if (remotePaths.isEmpty()) return
        viewModelScope.launch {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val treeDoc = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
            for (remotePath in remotePaths) {
                val fileName = remotePath.substringAfterLast("/")
                val doc = treeDoc?.createFile("application/octet-stream", fileName) ?: continue
                val destUri = doc.uri
                val taskId = enqueueDownload(remotePath, fileName, destUri)
                observeDownloadThenCopy(taskId, fileName, destUri)
            }
        }
    }

    private suspend fun enqueueDownload(remotePath: String, fileName: String, destUri: Uri): Long {
        return transferManager.enqueue(fileName, remotePath, destUri.toString(), 0, TransferDirection.DOWNLOAD)
    }

    private fun observeDownloadThenCopy(taskId: Long, fileName: String, destUri: Uri) {
        viewModelScope.launch {
            val items = transferManager.items.first { list ->
                when (list.find { it.id == taskId }?.status) {
                    TransferStatus.COMPLETED, TransferStatus.FAILED, TransferStatus.CANCELLED -> true
                    else -> false
                }
            }
            if (items.find { it.id == taskId }?.status == TransferStatus.COMPLETED) {
                val cacheFile = File(context.cacheDir, "sftping_dl_${taskId}_${fileName}")
                if (cacheFile.exists()) {
                    copyCacheToUri(cacheFile, destUri)
                    cacheFile.delete()
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

    private suspend fun resolveFileMeta(uri: Uri): Pair<String, Long> = withContext(Dispatchers.IO) {
        var name: String? = null
        var size = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex("_display_name")
                if (nameIdx >= 0) name = cursor.getString(nameIdx)
                val sizeIdx = cursor.getColumnIndex("_size")
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
        (name ?: uri.lastPathSegment ?: "file") to size
    }
}
