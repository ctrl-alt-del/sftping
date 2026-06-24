package com.example.sftping.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

private val Context.secretDataStore by preferencesDataStore(name = "secrets")

@Singleton
class SecretStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: KeystoreCrypto
) {
    suspend fun seal(id: String, secret: String) {
        val data = crypto.encrypt(secret)
        context.secretDataStore.edit { prefs ->
            prefs[stringPreferencesKey("${id}_iv")] = data.ivBase64
            prefs[stringPreferencesKey("${id}_data")] = data.ciphertextBase64
        }
    }

    suspend fun unseal(id: String): String? {
        val prefs = context.secretDataStore.data.firstOrNull() ?: return null
        val iv = prefs[stringPreferencesKey("${id}_iv")] ?: return null
        val ct = prefs[stringPreferencesKey("${id}_data")] ?: return null
        return try {
            crypto.decrypt(SecretData(iv, ct))
        } catch (_: Exception) {
            null
        }
    }

    suspend fun delete(id: String) {
        context.secretDataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey("${id}_iv"))
            prefs.remove(stringPreferencesKey("${id}_data"))
        }
    }
}
