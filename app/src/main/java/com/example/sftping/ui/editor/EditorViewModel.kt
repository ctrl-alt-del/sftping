package com.example.sftping.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sftping.data.editor.EditorLocation
import com.example.sftping.data.editor.EditorLocationRepository
import com.example.sftping.data.editor.PendingEdit
import com.example.sftping.data.editor.PendingEditDao
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.SessionState
import com.example.sftping.sftp.SftpException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SaveStatus {
    data object Idle : SaveStatus
    data object Saving : SaveStatus
    data class Saved(val at: Long) : SaveStatus
    data object PendingSync : SaveStatus
    data object NotConnected : SaveStatus
    data class Error(val message: String) : SaveStatus
}

data class EditorUiState(
    val locations: List<EditorLocation> = emptyList(),
    val pendingPaths: Set<String> = emptySet(),
    val openLocation: EditorLocation? = null,
    val content: String = "",
    val connected: Boolean = false,
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val error: String? = null,
    val showAddSheet: Boolean = false,
    val editingLocation: EditorLocation? = null
) {
    /** Editing is only allowed when a file is open with content loaded and the session is connected. */
    val editable: Boolean get() = openLocation != null && connected && loaded
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val sftpClient: ISftpClient,
    private val locationRepo: EditorLocationRepository,
    private val pendingEditDao: PendingEditDao,
    private val sessionState: SessionState
) : ViewModel() {

    /** Autosave debounce; overridable in tests. */
    internal var autosaveDelayMs: Long = 2_000L

    var uiState by mutableStateOf(EditorUiState())
        private set

    private val undoStack = UndoStack()
    private var autosaveJob: Job? = null

    init {
        viewModelScope.launch { reloadLocations() }
        viewModelScope.launch {
            pendingEditDao.observePendingPaths().collect { paths ->
                uiState = uiState.copy(pendingPaths = paths.toSet())
            }
        }
        viewModelScope.launch {
            sessionState.connected.collect { onConnectedChanged(it) }
        }
        viewModelScope.launch {
            sessionState.pendingEditPath.collect { path ->
                if (path != null) {
                    // Transient open handed over from the Files tab. Reacting to the
                    // StateFlow emission (instead of a screen re-entry hook) makes
                    // the handoff independent of composition timing. Consume it so a
                    // later emission reopens the newest path.
                    sessionState.clearPendingEdit()
                    open(EditorLocation.of(remotePath = path))
                }
            }
        }
    }

    // ---- Locations CRUD ----

    fun openAddSheet() { uiState = uiState.copy(showAddSheet = true, editingLocation = null) }
    fun closeAddSheet() { uiState = uiState.copy(showAddSheet = false, editingLocation = null) }
    fun startEditLocation(location: EditorLocation) {
        uiState = uiState.copy(showAddSheet = true, editingLocation = location)
    }

    fun addLocation(label: String, remotePath: String) {
        if (remotePath.isBlank()) return
        viewModelScope.launch {
            locationRepo.add(EditorLocation.of(remotePath = remotePath, label = label))
            reloadLocations()
            uiState = uiState.copy(showAddSheet = false, editingLocation = null)
        }
    }

    fun saveEditedLocation(id: String, label: String, remotePath: String) {
        if (remotePath.isBlank()) return
        viewModelScope.launch {
            val updated = EditorLocation.of(remotePath = remotePath, label = label, id = id)
            locationRepo.update(updated)
            reloadLocations()
            if (uiState.openLocation?.id == id) {
                uiState = uiState.copy(openLocation = updated)
            }
            uiState = uiState.copy(showAddSheet = false, editingLocation = null)
        }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch {
            val loc = uiState.locations.firstOrNull { it.id == id }
            locationRepo.remove(id)
            loc?.let { pendingEditDao.delete(it.remotePath) }
            if (uiState.openLocation?.id == id) close()
            reloadLocations()
        }
    }

    private suspend fun reloadLocations() {
        uiState = uiState.copy(locations = locationRepo.all())
    }

    // ---- Open / load ----

    fun open(location: EditorLocation) {
        autosaveJob?.cancel()
        viewModelScope.launch {
            uiState = uiState.copy(
                openLocation = location, loading = true, error = null, content = ""
            )
            val cached = pendingEditDao.get(location.remotePath)
            when {
                cached != null -> {
                    // Prefer un-synced local edits so offline work is never lost.
                    setLoadedContent(cached.content)
                    uiState = uiState.copy(
                        loading = false, saveStatus = SaveStatus.PendingSync, loaded = true
                    )
                }
                // Read the authoritative StateFlow value, not the UI mirror, so the
                // decision can't race the connected-collector's delivery.
                !sessionState.connected.value -> {
                    setLoadedContent("")
                    uiState = uiState.copy(
                        loading = false, saveStatus = SaveStatus.NotConnected, loaded = false
                    )
                }
                else -> {
                    try {
                        val text = sftpClient.readText(location.remotePath)
                        setLoadedContent(text)
                        uiState = uiState.copy(
                            loading = false, saveStatus = SaveStatus.Idle, loaded = true
                        )
                    } catch (e: SftpException) {
                        setLoadedContent("")
                        uiState = uiState.copy(
                            loading = false,
                            error = e.message ?: "Failed to open file",
                            saveStatus = SaveStatus.Error(e.message ?: "Failed to open file"),
                            loaded = false
                        )
                    } catch (e: IllegalStateException) {
                        // Dead session between the connected check and the read:
                        // not connected, no crash. Recovery happens on reconnect.
                        setLoadedContent("")
                        uiState = uiState.copy(
                            loading = false, saveStatus = SaveStatus.NotConnected, loaded = false
                        )
                    }
                }
            }
        }
    }

    private fun setLoadedContent(text: String) {
        undoStack.reset(text)
        uiState = uiState.copy(
            content = text, canUndo = undoStack.canUndo, canRedo = undoStack.canRedo
        )
    }

    fun close() {
        autosaveJob?.cancel()
        undoStack.reset("")
        uiState = uiState.copy(
            openLocation = null, content = "", canUndo = false, canRedo = false,
            saveStatus = SaveStatus.Idle, error = null, loaded = false
        )
    }

    // ---- Editing ----

    fun onContentChange(newValue: String) {
        if (!uiState.editable) return
        undoStack.push(newValue)
        uiState = uiState.copy(
            content = newValue, canUndo = undoStack.canUndo, canRedo = undoStack.canRedo
        )
        scheduleAutosave()
    }

    fun undo() {
        if (!undoStack.canUndo) return
        val value = undoStack.undo()
        uiState = uiState.copy(
            content = value, canUndo = undoStack.canUndo, canRedo = undoStack.canRedo
        )
        scheduleAutosave()
    }

    fun redo() {
        if (!undoStack.canRedo) return
        val value = undoStack.redo()
        uiState = uiState.copy(
            content = value, canUndo = undoStack.canUndo, canRedo = undoStack.canRedo
        )
        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(autosaveDelayMs)
            saveNow()
        }
    }

    /** Force an immediate save (manual Save icon or debounce expiry). */
    fun saveNow() {
        autosaveJob?.cancel()
        val location = uiState.openLocation ?: return
        viewModelScope.launch { doSave(location) }
    }

    private suspend fun doSave(location: EditorLocation) {
        val content = uiState.content
        if (!uiState.connected) {
            cacheEdit(location, content)
            return
        }
        uiState = uiState.copy(saveStatus = SaveStatus.Saving)
        try {
            sftpClient.writeText(location.remotePath, content)
            pendingEditDao.delete(location.remotePath)
            uiState = uiState.copy(saveStatus = SaveStatus.Saved(System.currentTimeMillis()), error = null)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) {
                // A permission failure won't resolve by retrying later, so surface a
                // clear error instead of silently hoarding an unsyncable pending edit.
                android.util.Log.w("EditorViewModel", "Save denied by server", e)
                val msg = "Permission denied — no write access to this file"
                uiState = uiState.copy(saveStatus = SaveStatus.Error(msg), error = msg)
            } else {
                // Connectivity / transient failure: cache offline for later flush.
                android.util.Log.w("EditorViewModel", "Save failed, caching offline", e)
                cacheEdit(location, content)
            }
        }
    }

    private fun isPermissionDenied(e: Throwable): Boolean {
        // Prefer the structured flag from the SFTP layer (JSch status id == 3);
        // fall back to message scanning for any non-SftpException failure path.
        val fromStatus = generateSequence(e) { it.cause }
            .filterIsInstance<SftpException>()
            .any { it.permissionDenied }
        if (fromStatus) return true
        val text = generateSequence(e) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" ")
            .lowercase()
        return "permission denied" in text || "access denied" in text
    }

    private suspend fun cacheEdit(location: EditorLocation, content: String) {
        pendingEditDao.upsert(PendingEdit(location.remotePath, content))
        uiState = uiState.copy(saveStatus = SaveStatus.PendingSync)
    }

    // ---- Connection changes / reconnect flush ----

    private suspend fun onConnectedChanged(nowConnected: Boolean) {
        val wasConnected = uiState.connected
        uiState = uiState.copy(connected = nowConnected)
        val location = uiState.openLocation
        if (!wasConnected && nowConnected && location != null) {
            // Reconnected: flush any pending offline edits for the open file.
            val pending = pendingEditDao.get(location.remotePath)
            when {
                pending != null -> doSave(location)
                // The open landed in NotConnected before the session was up:
                // re-run the cache-aware load now that connectivity is confirmed.
                // Guarded by loaded so an in-memory edit of a loaded file is kept.
                !uiState.loaded && uiState.saveStatus is SaveStatus.NotConnected ->
                    open(location)
            }
        } else if (!nowConnected && location != null && uiState.saveStatus !is SaveStatus.PendingSync) {
            uiState = uiState.copy(saveStatus = SaveStatus.NotConnected)
        }
    }

    fun clearError() { uiState = uiState.copy(error = null) }
}
