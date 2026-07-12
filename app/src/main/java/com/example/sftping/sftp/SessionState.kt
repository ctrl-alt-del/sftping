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

    private val _connected = MutableStateFlow(false)

    /** Reactive connection state: true while an SFTP session is live. */
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    fun setConnected(value: Boolean) {
        _connected.value = value
    }
}
