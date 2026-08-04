package com.example.sftping.sftp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionState @Inject constructor() {
    @Volatile
    var initialDirectory: String = "/"

    // Incremented on each successful connect so screens can detect a new session.
    @Volatile
    var epoch: Int = 0

    private val _pendingEditPath = MutableStateFlow<String?>(null)

    /**
     * Remote path handed from the Files tab to the Editor tab for a transient
     * open. A StateFlow so the Editor can react to the emission directly instead
     * of relying on screen re-entry timing; the Editor consumes it (sets back to
     * null) once opened.
     */
    val pendingEditPath: StateFlow<String?> = _pendingEditPath.asStateFlow()

    fun setPendingEdit(path: String) {
        android.util.Log.i("EditHandoff", "setPendingEdit($path)")
        _pendingEditPath.value = path
    }

    fun clearPendingEdit() {
        android.util.Log.i("EditHandoff", "clearPendingEdit()")
        _pendingEditPath.value = null
    }

    private val _connected = MutableStateFlow(false)

    /** Reactive connection state: true while an SFTP session is live. */
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    fun setConnected(value: Boolean) {
        _connected.value = value
    }
}
