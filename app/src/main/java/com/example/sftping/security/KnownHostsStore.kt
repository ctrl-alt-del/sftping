package com.example.sftping.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface KnownHostsStore {
    suspend fun get(host: String): String?
    suspend fun put(host: String, fingerprint: String, keyType: String = "")
    suspend fun remove(host: String)
    suspend fun all(): List<TrustedHost>
}

@Singleton
class InMemoryKnownHostsStore @Inject constructor() : KnownHostsStore {
    private val store = mutableMapOf<String, TrustedHost>()

    override suspend fun get(host: String): String? = store[host]?.fingerprint

    override suspend fun put(host: String, fingerprint: String, keyType: String) {
        store[host] = TrustedHost(host, fingerprint, keyType, System.currentTimeMillis())
    }

    override suspend fun remove(host: String) {
        store.remove(host)
    }

    override suspend fun all(): List<TrustedHost> = store.values.toList()
}

private val Context.knownHostsDataStore by preferencesDataStore(name = "known_hosts")

@Singleton
class DataStoreKnownHostsStore @Inject constructor(
    @ApplicationContext private val context: Context
) : KnownHostsStore {
    private val hostsKey = stringPreferencesKey("trusted_hosts")

    override suspend fun get(host: String): String? =
        all().firstOrNull { it.host == host }?.fingerprint

    override suspend fun put(host: String, fingerprint: String, keyType: String) {
        val updated = all().filterNot { it.host == host } +
            TrustedHost(host, fingerprint, keyType, System.currentTimeMillis())
        save(updated)
    }

    override suspend fun remove(host: String) {
        save(all().filterNot { it.host == host })
    }

    override suspend fun all(): List<TrustedHost> {
        val json = context.knownHostsDataStore.data.first()[hostsKey] ?: "[]"
        return try {
            TrustedHost.listFromJson(json)
        } catch (e: Exception) {
            android.util.Log.w("KnownHostsStore", "Failed to parse trusted hosts", e)
            emptyList()
        }
    }

    private suspend fun save(hosts: List<TrustedHost>) {
        context.knownHostsDataStore.edit { prefs ->
            prefs[hostsKey] = TrustedHost.listToJson(hosts)
        }
    }
}
