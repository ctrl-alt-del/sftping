package com.example.sftping.transfer

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferManagerTest {

    @Test
    fun `add and observe items`() = runTest {
        val manager = TransferManager()
        val id = manager.createId()
        manager.add(
            TransferItem(id, "test.mp4", "/home/test.mp4", TransferDirection.DOWNLOAD, 1000, 0, TransferStatus.RUNNING)
        )
        val items = manager.items.first()
        assertEquals(1, items.size)
        assertEquals("test.mp4", items[0].fileName)
    }

    @Test
    fun `updateProgress updates transferred and total`() = runTest {
        val manager = TransferManager()
        val id = manager.createId()
        manager.add(
            TransferItem(id, "f", "/f", TransferDirection.UPLOAD, 5000, 0, TransferStatus.RUNNING)
        )
        manager.updateProgress(id, 2500, 5000, 1024L)
        val item = manager.items.first().first()
        assertEquals(2500L, item.transferredBytes)
        assertEquals(5000L, item.totalBytes)
        assertEquals(1024L, item.speed)
    }

    @Test
    fun `mark completes item`() = runTest {
        val manager = TransferManager()
        val id = manager.createId()
        manager.add(
            TransferItem(id, "f", "/f", TransferDirection.DOWNLOAD, 3000, 3000, TransferStatus.RUNNING)
        )
        manager.mark(id, TransferStatus.COMPLETED)
        assertEquals(TransferStatus.COMPLETED, manager.items.first().first().status)
    }
}
