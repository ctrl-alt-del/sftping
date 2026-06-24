package com.example.sftping.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class KeystoreCryptoTest {

    @Test
    fun roundTripEncryptDecrypt() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val crypto = KeystoreCrypto(keyStore)
        val original = "s3cret_p@ss!"
        val encrypted = crypto.encrypt(original)
        val decrypted = crypto.decrypt(encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun encryptIsNonDeterministic() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val crypto = KeystoreCrypto(keyStore)
        val a = crypto.encrypt("test")
        val b = crypto.encrypt("test")
        assertNotEquals(a.ciphertextBase64, b.ciphertextBase64)
    }
}
