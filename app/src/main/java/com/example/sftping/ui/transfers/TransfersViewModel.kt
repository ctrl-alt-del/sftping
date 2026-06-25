package com.example.sftping.ui.transfers

import androidx.lifecycle.ViewModel
import com.example.sftping.transfer.TransferManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    manager: TransferManager
) : ViewModel() {
    val items: StateFlow<List<com.example.sftping.transfer.TransferItem>> = manager.items
}
