package com.example.sftping.data.editor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PendingEditDaoTest {

    @Test
    fun `upsert then get returns the edit`() = runTest {
        val dao = FakePendingEditDao()
        val edit = PendingEdit(remotePath = "/etc/app.conf", content = "hello", updatedAt = 1L)
        dao.upsert(edit)
        assertEquals(edit, dao.get("/etc/app.conf"))
    }

    @Test
    fun `upsert replaces content by remote path`() = runTest {
        val dao = FakePendingEditDao()
        dao.upsert(PendingEdit(remotePath = "/f", content = "v1", updatedAt = 1L))
        dao.upsert(PendingEdit(remotePath = "/f", content = "v2", updatedAt = 2L))
        assertEquals("v2", dao.get("/f")?.content)
        assertEquals(1, dao.all().size)
    }

    @Test
    fun `delete removes the edit`() = runTest {
        val dao = FakePendingEditDao()
        dao.upsert(PendingEdit(remotePath = "/f", content = "v", updatedAt = 1L))
        dao.delete("/f")
        assertNull(dao.get("/f"))
    }

    @Test
    fun `get for missing path is null`() = runTest {
        val dao = FakePendingEditDao()
        assertNull(dao.get("/nope"))
    }

    @Test
    fun `observePendingPaths reflects current keys`() = runTest {
        val dao = FakePendingEditDao()
        dao.upsert(PendingEdit(remotePath = "/a", content = "a", updatedAt = 1L))
        dao.upsert(PendingEdit(remotePath = "/b", content = "b", updatedAt = 2L))
        assertEquals(setOf("/a", "/b"), dao.observePendingPaths().first().toSet())
    }
}
