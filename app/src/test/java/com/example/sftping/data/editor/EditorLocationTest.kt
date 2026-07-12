package com.example.sftping.data.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorLocationTest {

    @Test
    fun `list round-trips through json`() {
        val list = listOf(
            EditorLocation("id1", "nginx.conf", "/etc/nginx/nginx.conf", 100L),
            EditorLocation("id2", ".env", "/srv/app/.env", 200L)
        )
        val json = EditorLocation.listToJson(list)
        val restored = EditorLocation.listFromJson(json)
        assertEquals(list, restored)
    }

    @Test
    fun `empty json array yields empty list`() {
        assertTrue(EditorLocation.listFromJson("[]").isEmpty())
    }

    @Test
    fun `blank json yields empty list`() {
        assertTrue(EditorLocation.listFromJson("").isEmpty())
    }

    @Test
    fun `of defaults blank label to file name`() {
        val loc = EditorLocation.of(remotePath = "/etc/nginx/nginx.conf", label = "")
        assertEquals("nginx.conf", loc.label)
        assertEquals("/etc/nginx/nginx.conf", loc.remotePath)
    }

    @Test
    fun `of keeps explicit label`() {
        val loc = EditorLocation.of(remotePath = "/srv/app/.env", label = "App env")
        assertEquals("App env", loc.label)
    }

    @Test
    fun `of trims trailing slash when deriving label`() {
        val loc = EditorLocation.of(remotePath = "/var/www/html/", label = "")
        assertEquals("html", loc.label)
    }

    @Test
    fun `of generates unique ids`() {
        val a = EditorLocation.of(remotePath = "/a.txt")
        val b = EditorLocation.of(remotePath = "/a.txt")
        assertTrue(a.id != b.id)
    }
}
