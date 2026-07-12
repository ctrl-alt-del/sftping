package com.example.sftping.data.editor

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** CRUD for the user's saved editor locations. */
interface EditorLocationRepository {
    suspend fun all(): List<EditorLocation>
    suspend fun add(location: EditorLocation)
    suspend fun update(location: EditorLocation)
    suspend fun remove(id: String)
}

/** Fast in-memory double for unit tests (mirrors InMemoryKnownHostsStore). */
class InMemoryEditorLocationRepository : EditorLocationRepository {
    private val store = linkedMapOf<String, EditorLocation>()
    override suspend fun all(): List<EditorLocation> = store.values.toList()
    override suspend fun add(location: EditorLocation) { store[location.id] = location }
    override suspend fun update(location: EditorLocation) { store[location.id] = location }
    override suspend fun remove(id: String) { store.remove(id) }
}

private val Context.editorLocationsDataStore by preferencesDataStore(name = "editor_locations")

/** Persists the list of saved editor locations as a JSON blob in DataStore. */
@Singleton
class DataStoreEditorLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : EditorLocationRepository {
    private val locationsKey = stringPreferencesKey("locations")

    override suspend fun all(): List<EditorLocation> {
        val json = context.editorLocationsDataStore.data.first()[locationsKey] ?: "[]"
        return try {
            EditorLocation.listFromJson(json)
        } catch (e: Exception) {
            android.util.Log.w("EditorLocationRepo", "Failed to parse editor locations", e)
            emptyList()
        }
    }

    override suspend fun add(location: EditorLocation) {
        save(all().filterNot { it.id == location.id } + location)
    }

    override suspend fun update(location: EditorLocation) {
        save(all().map { if (it.id == location.id) location else it })
    }

    override suspend fun remove(id: String) {
        save(all().filterNot { it.id == id })
    }

    private suspend fun save(locations: List<EditorLocation>) {
        context.editorLocationsDataStore.edit { prefs ->
            prefs[locationsKey] = EditorLocation.listToJson(locations)
        }
    }
}
