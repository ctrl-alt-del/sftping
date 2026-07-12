package com.example.sftping.data.editor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * In-memory [PendingEditDao] test double. Room DAOs require an instrumented test
 * to exercise the real SQLite implementation, so — consistent with the project's
 * FakeDao pattern for TransferTaskDao — pure JVM tests use this fake, which
 * mirrors the REPLACE-by-primary-key semantics the ViewModel depends on.
 */
class FakePendingEditDao : PendingEditDao {
    private val store = linkedMapOf<String, PendingEdit>()
    private val flow = MutableSharedFlow<List<String>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init { flow.tryEmit(emptyList()) }

    override suspend fun upsert(edit: PendingEdit) {
        store[edit.remotePath] = edit
        flow.tryEmit(store.keys.toList())
    }

    override suspend fun get(remotePath: String): PendingEdit? = store[remotePath]

    override suspend fun all(): List<PendingEdit> = store.values.toList()

    override fun observePendingPaths(): Flow<List<String>> = flow

    override suspend fun delete(remotePath: String) {
        store.remove(remotePath)
        flow.tryEmit(store.keys.toList())
    }
}
