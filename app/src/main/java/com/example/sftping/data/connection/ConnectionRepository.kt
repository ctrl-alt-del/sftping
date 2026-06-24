package com.example.sftping.data.connection

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "connection_settings")

@Singleton
class ConnectionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recentKey = stringPreferencesKey("recent_connections")

    suspend fun loadRecent(): List<ConnectionProfile> {
        val json = context.dataStore.data.map { prefs ->
            prefs[recentKey] ?: "[]"
        }.first()
        return ConnectionProfile.listFromJson(json)
    }

    suspend fun saveRecent(profiles: List<ConnectionProfile>) {
        context.dataStore.edit { prefs ->
            prefs[recentKey] = ConnectionProfile.listToJson(profiles)
        }
    }

    suspend fun addRecent(profile: ConnectionProfile) {
        val recent = loadRecent().toMutableList()
        recent.removeAll { it.host == profile.host && it.username == profile.username }
        recent.add(0, profile.copy(lastConnected = System.currentTimeMillis()))
        if (recent.size > 20) {
            recent.subList(20, recent.size).clear()
        }
        saveRecent(recent)
    }
}
