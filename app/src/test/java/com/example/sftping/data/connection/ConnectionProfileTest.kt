package com.example.sftping.data.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionProfileTest {

    @Test
    fun `toJson and fromJson roundtrip`() {
        val original = ConnectionProfile("192.168.1.10", 2222, "admin", "prod1", 1712928000000L)
        val json = original.toJson()
        val restored = ConnectionProfile.fromJson(json)
        assertEquals(original, restored)
    }

    @Test
    fun `fromJson with missing fields uses defaults`() {
        val json = org.json.JSONObject("{\"host\":\"10.0.0.1\"}")
        val profile = ConnectionProfile.fromJson(json)
        assertEquals("10.0.0.1", profile.host)
        assertEquals(22, profile.port)
        assertEquals("", profile.username)
    }

    @Test
    fun `list toJson and fromJson roundtrip`() {
        val profiles = listOf(
            ConnectionProfile("a", 22, "x"),
            ConnectionProfile("b", 33, "y", lastConnected = 1L)
        )
        val json = ConnectionProfile.listToJson(profiles)
        val restored = ConnectionProfile.listFromJson(json)
        assertEquals(profiles, restored)
    }

    @Test
    fun `listFromJson of empty array returns empty list`() {
        val result = ConnectionProfile.listFromJson("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `defaultDirectory round-trips`() {
        val original = ConnectionProfile("h", 22, "u", defaultDirectory = "/var/www")
        val restored = ConnectionProfile.fromJson(original.toJson())
        assertEquals("/var/www", restored.defaultDirectory)
        assertEquals(original, restored)
    }

    @Test
    fun `fromJson without defaultDirectory defaults to empty`() {
        val profile = ConnectionProfile.fromJson(org.json.JSONObject("{\"host\":\"h\"}"))
        assertEquals("", profile.defaultDirectory)
    }
}
