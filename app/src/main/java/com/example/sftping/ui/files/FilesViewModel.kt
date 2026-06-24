package com.example.sftping.ui.files

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.RemoteFile
import com.example.sftping.sftp.SftpException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilesUiState(
    val currentPath: String = "/",
    val files: List<RemoteFile> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val pathStack: List<String> = emptyList()
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val sftpClient: ISftpClient
) : ViewModel() {

    var uiState by mutableStateOf(FilesUiState())
        private set

    private val _navigateToConnection = MutableSharedFlow<Unit>()
    val navigateToConnection: SharedFlow<Unit> = _navigateToConnection

    fun loadFiles(path: String = "/") {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            try {
                val files = sftpClient.listFiles(path)
                val sorted = files.sortedWith(
                    compareByDescending<RemoteFile> { it.isDirectory }.thenBy { it.name.lowercase() }
                )
                uiState = uiState.copy(
                    currentPath = path,
                    files = sorted,
                    loading = false
                )
            } catch (e: SftpException) {
                uiState = uiState.copy(loading = false, error = e.message ?: "Failed to list files")
            } catch (e: IllegalStateException) {
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
        uiState = uiState.copy(pathStack = uiState.pathStack + uiState.currentPath)
        loadFiles(newPath)
    }

    fun navigateBack() {
        val stack = uiState.pathStack
        if (stack.isEmpty()) return
        val previous = stack.last()
        uiState = uiState.copy(pathStack = stack.dropLast(1))
        loadFiles(previous)
    }

    fun canGoBack(): Boolean = uiState.pathStack.isNotEmpty()

    fun clearError() { uiState = uiState.copy(error = null) }
}
