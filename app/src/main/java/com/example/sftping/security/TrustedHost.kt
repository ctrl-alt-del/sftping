package com.example.sftping.security

import org.json.JSONArray
import org.json.JSONObject

data class TrustedHost(
    val host: String = "",
    val fingerprint: String = "",
    val keyType: String = "",
    val trustedAt: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("host", host)
        put("fingerprint", fingerprint)
        put("keyType", keyType)
        put("trustedAt", trustedAt)
    }

    companion object {
        fun fromJson(json: JSONObject) = TrustedHost(
            host = json.optString("host", ""),
            fingerprint = json.optString("fingerprint", ""),
            keyType = json.optString("keyType", ""),
            trustedAt = json.optLong("trustedAt", 0L)
        )

        fun listToJson(hosts: List<TrustedHost>): String =
            JSONArray().apply { hosts.forEach { put(it.toJson()) } }.toString()

        fun listFromJson(json: String): List<TrustedHost> {
            val array = JSONArray(json)
            return (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }
    }
}
