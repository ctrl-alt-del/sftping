package com.example.sftping.ui.files

import com.example.sftping.sftp.RemoteFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileViewTest {

    private fun file(name: String, size: Long = 0, mtime: Long = 0, dir: Boolean = false) =
        RemoteFile(name, "/$name", size, mtime, dir)

    private val sample = listOf(
        file("zebra.txt", size = 10, mtime = 5),
        file("Apple", dir = true),
        file(".bashrc", size = 1, mtime = 9),
        file("beta.log", size = 100, mtime = 1),
        file("alpha.LOG", size = 50, mtime = 7),
        file("docs", dir = true)
    )

    @Test
    fun `hides dot-files by default`() {
        val result = FileView.apply(sample, showHidden = false, query = "", sortMode = SortMode.NAME_ASC)
        assertFalse(result.any { it.name == ".bashrc" })
    }

    @Test
    fun `shows dot-files when enabled`() {
        val result = FileView.apply(sample, showHidden = true, query = "", sortMode = SortMode.NAME_ASC)
        assertTrue(result.any { it.name == ".bashrc" })
    }

    @Test
    fun `search filters by name case-insensitively`() {
        val result = FileView.apply(sample, showHidden = false, query = "log", sortMode = SortMode.NAME_ASC)
        assertEquals(setOf("beta.log", "alpha.LOG"), result.map { it.name }.toSet())
    }

    @Test
    fun `folders always come first`() {
        val result = FileView.apply(sample, showHidden = false, query = "", sortMode = SortMode.SIZE)
        val lastDir = result.indexOfLast { it.isDirectory }
        val firstNonDir = result.indexOfFirst { !it.isDirectory }
        assertTrue(lastDir < firstNonDir)
    }

    @Test
    fun `name ascending and descending are reverses within files`() {
        val asc = FileView.apply(sample, showHidden = false, query = "", sortMode = SortMode.NAME_ASC)
            .filter { !it.isDirectory }.map { it.name }
        val desc = FileView.apply(sample, showHidden = false, query = "", sortMode = SortMode.NAME_DESC)
            .filter { !it.isDirectory }.map { it.name }
        assertEquals(listOf("alpha.LOG", "beta.log", "zebra.txt"), asc)
        assertEquals(asc, desc.reversed())
    }

    @Test
    fun `size sorts largest first`() {
        val result = FileView.apply(sample, showHidden = false, query = "", sortMode = SortMode.SIZE)
            .filter { !it.isDirectory }.map { it.name }
        assertEquals(listOf("beta.log", "alpha.LOG", "zebra.txt"), result)
    }

    @Test
    fun `last modified sorts newest first`() {
        val result = FileView.apply(sample, showHidden = true, query = "", sortMode = SortMode.LAST_MODIFIED)
            .filter { !it.isDirectory }.map { it.name }
        assertEquals(listOf(".bashrc", "alpha.LOG", "zebra.txt", "beta.log"), result)
    }
}
