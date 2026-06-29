package com.example.sftping.ui.transfers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sftping.transfer.TransferManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val manager: TransferManager
) : ViewModel() {
    val items: StateFlow<List<com.example.sftping.transfer.TransferItem>> = manager.items

    fun cancel(id: Long) {
        viewModelScope.launch { manager.cancel(id) }
    }

    fun retry(id: Long) {
        viewModelScope.launch { manager.retry(id) }
    }
}
