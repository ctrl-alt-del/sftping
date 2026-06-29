package com.example.sftping.data.connection

import org.json.JSONArray
import org.json.JSONObject

data class ConnectionProfile(
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val nickname: String = "",
    val lastConnected: Long = 0L,
    val defaultDirectory: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("host", host)
        put("port", port)
        put("username", username)
        put("nickname", nickname)
        put("lastConnected", lastConnected)
        put("defaultDirectory", defaultDirectory)
    }

    companion object {
        fun fromJson(json: JSONObject) = ConnectionProfile(
            host = json.optString("host", ""),
            port = json.optInt("port", 22),
            username = json.optString("username", ""),
            nickname = json.optString("nickname", ""),
            lastConnected = json.optLong("lastConnected", 0L),
            defaultDirectory = json.optString("defaultDirectory", "")
        )

        fun listToJson(profiles: List<ConnectionProfile>): String =
            JSONArray().apply { profiles.forEach { put(it.toJson()) } }.toString()

        fun listFromJson(json: String): List<ConnectionProfile> {
            val array = JSONArray(json)
            return (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }
    }
}
