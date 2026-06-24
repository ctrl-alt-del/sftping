package com.example.sftping.security

import javax.inject.Inject
import javax.inject.Singleton

interface KnownHostsStore {
    fun get(host: String): String?
    fun put(host: String, fingerprint: String)
}

@Singleton
class InMemoryKnownHostsStore @Inject constructor() : KnownHostsStore {
    private val store = mutableMapOf<String, String>()

    override fun get(host: String): String? = store[host]

    override fun put(host: String, fingerprint: String) {
        store[host] = fingerprint
    }
}
