package com.example.sftping.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferManager @Inject constructor() {

    private val _items = MutableStateFlow<List<TransferItem>>(emptyList())
    val items: StateFlow<List<TransferItem>> = _items

    private var nextId = 0L

    fun createId(): Long = ++nextId

    fun add(item: TransferItem) {
        _items.update { it + item }
    }

    fun updateProgress(id: Long, transferred: Long, total: Long, speed: Long = 0L) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(transferredBytes = transferred, totalBytes = total, speed = speed) else it }
        }
    }

    fun mark(id: Long, status: TransferStatus) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(status = status) else it }
        }
    }
}
