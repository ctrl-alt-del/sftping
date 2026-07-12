package com.example.sftping.data.editor

import org.json.JSONArray
import org.json.JSONObject

data class EditorLocation(
    val id: String = "",
    val label: String = "",
    val remotePath: String = "",
    val addedAt: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("remotePath", remotePath)
        put("addedAt", addedAt)
    }

    companion object {
        /**
         * Build a location, defaulting a blank [label] to the file name (last path
         * segment) of [remotePath].
         */
        fun of(
            remotePath: String,
            label: String = "",
            id: String = java.util.UUID.randomUUID().toString(),
            addedAt: Long = System.currentTimeMillis()
        ): EditorLocation {
            val trimmedPath = remotePath.trim()
            val resolvedLabel = label.trim().ifEmpty {
                trimmedPath.trimEnd('/').substringAfterLast('/').ifEmpty { trimmedPath }
            }
            return EditorLocation(id, resolvedLabel, trimmedPath, addedAt)
        }

        fun fromJson(json: JSONObject) = EditorLocation(
            id = json.optString("id", ""),
            label = json.optString("label", ""),
            remotePath = json.optString("remotePath", ""),
            addedAt = json.optLong("addedAt", 0L)
        )

        fun listToJson(locations: List<EditorLocation>): String =
            JSONArray().apply { locations.forEach { put(it.toJson()) } }.toString()

        fun listFromJson(json: String): List<EditorLocation> {
            if (json.isBlank()) return emptyList()
            val array = JSONArray(json)
            return (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }
    }
}
