package com.example.sftping.security

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KnownHostsStoreTest {

    @Test
    fun `put and get fingerprint`() = runTest {
        val store = InMemoryKnownHostsStore()
        assertNull(store.get("example.com"))
        store.put("example.com", "SHA256:abc123", "ssh-ed25519")
        assertEquals("SHA256:abc123", store.get("example.com"))
    }

    @Test
    fun `overwrite replaces old fingerprint`() = runTest {
        val store = InMemoryKnownHostsStore()
        store.put("host", "SHA256:old", "ssh-rsa")
        store.put("host", "SHA256:new", "ssh-rsa")
        assertEquals("SHA256:new", store.get("host"))
    }

    @Test
    fun `remove deletes the entry`() = runTest {
        val store = InMemoryKnownHostsStore()
        store.put("host", "SHA256:x", "ssh-rsa")
        store.remove("host")
        assertNull(store.get("host"))
        assertTrue(store.all().isEmpty())
    }

    @Test
    fun `remove of unknown host is a no-op`() = runTest {
        val store = InMemoryKnownHostsStore()
        store.remove("nope")
        assertTrue(store.all().isEmpty())
    }

    @Test
    fun `all returns trusted hosts with metadata`() = runTest {
        val store = InMemoryKnownHostsStore()
        store.put("a", "SHA256:a", "ssh-ed25519")
        store.put("b", "SHA256:b", "ssh-rsa")
        val all = store.all()
        assertEquals(2, all.size)
        assertEquals(setOf("a", "b"), all.map { it.host }.toSet())
        assertEquals("ssh-ed25519", all.first { it.host == "a" }.keyType)
    }
}
