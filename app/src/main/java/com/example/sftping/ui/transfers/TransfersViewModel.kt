package com.example.sftping.ui.transfers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sftping.sftp.SessionState
import com.example.sftping.transfer.TransferManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val manager: TransferManager,
    private val sessionState: SessionState
) : ViewModel() {
    val items: StateFlow<List<com.example.sftping.transfer.TransferItem>> = manager.items

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    init {
        viewModelScope.launch {
            sessionState.connected.collect { connected ->
                _connected.value = connected
            }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch { manager.cancel(id) }
    }

    fun retry(id: Long) {
        viewModelScope.launch { manager.retry(id) }
    }

    fun retryAllFailed() {
        viewModelScope.launch { manager.retryAllFailed() }
    }
}
