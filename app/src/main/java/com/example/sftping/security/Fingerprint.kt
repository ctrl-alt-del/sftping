package com.example.sftping.security

import java.security.MessageDigest
import java.util.Base64

object Fingerprint {
    fun sha256(keyBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(keyBytes)
        return "SHA256:" + Base64.getEncoder().encodeToString(digest)
    }
}
