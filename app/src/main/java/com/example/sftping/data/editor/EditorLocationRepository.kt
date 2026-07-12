package com.example.sftping.data.editor

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.editorLocationsDataStore by preferencesDataStore(name = "editor_locations")

/** Persists the list of saved editor locations as a JSON blob in DataStore. */
@Singleton
class EditorLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationsKey = stringPreferencesKey("locations")

    suspend fun all(): List<EditorLocation> {
        val json = context.editorLocationsDataStore.data.first()[locationsKey] ?: "[]"
        return try {
            EditorLocation.listFromJson(json)
        } catch (e: Exception) {
            android.util.Log.w("EditorLocationRepo", "Failed to parse editor locations", e)
            emptyList()
        }
    }

    suspend fun add(location: EditorLocation) {
        save(all().filterNot { it.id == location.id } + location)
    }

    suspend fun update(location: EditorLocation) {
        save(all().map { if (it.id == location.id) location else it })
    }

    suspend fun remove(id: String) {
        save(all().filterNot { it.id == id })
    }

    private suspend fun save(locations: List<EditorLocation>) {
        context.editorLocationsDataStore.edit { prefs ->
            prefs[locationsKey] = EditorLocation.listToJson(locations)
        }
    }
}
