package com.example.sftping.ui.files

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class UploadCandidateTest {

    @Test
    fun `uploadRemotePath joins under root and nested dirs`() {
        assertEquals("/a.txt", uploadRemotePath("/", "a.txt"))
        assertEquals("/d/a.txt", uploadRemotePath("/d", "a.txt"))
    }

    @Test
    fun `markUploaded flags names already uploaded into current dir`() {
        val result = markUploaded(
            names = listOf("a.txt", "b.txt"),
            currentPath = "/d",
            uploadedPaths = setOf("/d/a.txt")
        )
        assertEquals(listOf(true, false), result)
    }

    @Test
    fun `markUploaded is path-scoped`() {
        val result = markUploaded(
            names = listOf("a.txt"),
            currentPath = "/other",
            uploadedPaths = setOf("/d/a.txt")
        )
        assertEquals(listOf(false), result)
    }

    @Test
    fun `UploadCandidate is selected by default`() {
        val c = UploadCandidate(mock<Uri>(), "a.txt", 10, alreadyUploaded = false)
        assertTrue(c.selected)
        assertFalse(c.alreadyUploaded)
    }
}
