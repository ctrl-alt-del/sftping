package com.example.sftping.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustedHostTest {

    @Test
    fun `list round-trips through json`() {
        val hosts = listOf(
            TrustedHost("example.com", "SHA256:abc", "ssh-ed25519", 1000L),
            TrustedHost("10.0.0.1", "SHA256:def", "ssh-rsa", 2000L)
        )
        val json = TrustedHost.listToJson(hosts)
        assertEquals(hosts, TrustedHost.listFromJson(json))
    }

    @Test
    fun `empty list round-trips`() {
        val json = TrustedHost.listToJson(emptyList())
        assertTrue(TrustedHost.listFromJson(json).isEmpty())
    }

    @Test
    fun `missing fields fall back to defaults`() {
        val obj = org.json.JSONObject().apply { put("host", "h") }
        val parsed = TrustedHost.fromJson(obj)
        assertEquals("h", parsed.host)
        assertEquals("", parsed.fingerprint)
        assertEquals("", parsed.keyType)
        assertEquals(0L, parsed.trustedAt)
    }

    @Test(expected = org.json.JSONException::class)
    fun `malformed json throws`() {
        TrustedHost.listFromJson("not json")
    }
}
