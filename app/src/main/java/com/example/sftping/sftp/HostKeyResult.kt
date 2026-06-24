package com.example.sftping.sftp

sealed class HostKeyResult {
    data object Trusted : HostKeyResult()
    data class Unknown(
        val host: String,
        val fingerprint: String,
        val keyType: String
    ) : HostKeyResult()

    data class Changed(
        val host: String,
        val storedFingerprint: String,
        val presentedFingerprint: String
    ) : HostKeyResult()
}
