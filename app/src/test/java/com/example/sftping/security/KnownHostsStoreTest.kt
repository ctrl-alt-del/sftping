package com.example.sftping.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KnownHostsStoreTest {

    @Test
    fun `put and get fingerprint`() {
        val store = InMemoryKnownHostsStore()
        assertNull(store.get("example.com"))
        store.put("example.com", "SHA256:abc123")
        assertEquals("SHA256:abc123", store.get("example.com"))
    }

    @Test
    fun `overwrite replaces old fingerprint`() {
        val store = InMemoryKnownHostsStore()
        store.put("host", "SHA256:old")
        store.put("host", "SHA256:new")
        assertEquals("SHA256:new", store.get("host"))
    }
}
