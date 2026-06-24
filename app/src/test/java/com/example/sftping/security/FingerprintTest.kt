package com.example.sftping.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FingerprintTest {

    @Test
    fun `sha256 produces expected format`() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val fp = Fingerprint.sha256(bytes)
        assertTrue(fp.startsWith("SHA256:"))
        assertEquals(51, fp.length) // "SHA256:" + 44 base64 chars
    }

    @Test
    fun `sha256 is deterministic`() {
        val bytes = byteArrayOf(42, 7, 99)
        assertEquals(Fingerprint.sha256(bytes), Fingerprint.sha256(bytes))
    }

    @Test
    fun `sha256 differs for different inputs`() {
        val a = Fingerprint.sha256(byteArrayOf(1, 2, 3))
        val b = Fingerprint.sha256(byteArrayOf(1, 2, 4))
        assertTrue(a != b)
    }
}
