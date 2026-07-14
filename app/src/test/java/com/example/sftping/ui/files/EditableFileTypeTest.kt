package com.example.sftping.ui.files

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditableFileTypeTest {

    @Test
    fun `common text extensions are editable`() {
        assertTrue(EditableFileType.isEditable("app.conf"))
        assertTrue(EditableFileType.isEditable("notes.md"))
        assertTrue(EditableFileType.isEditable("data.json"))
        assertTrue(EditableFileType.isEditable("script.sh"))
        assertTrue(EditableFileType.isEditable("Main.kt"))
    }

    @Test
    fun `extension check is case-insensitive`() {
        assertTrue(EditableFileType.isEditable("NOTES.MD"))
        assertTrue(EditableFileType.isEditable("Config.JSON"))
    }

    @Test
    fun `binary and archive types are not editable`() {
        assertFalse(EditableFileType.isEditable("photo.PNG"))
        assertFalse(EditableFileType.isEditable("archive.zip"))
        assertFalse(EditableFileType.isEditable("movie.mp4"))
    }

    @Test
    fun `known dotless filenames are editable`() {
        assertTrue(EditableFileType.isEditable("README"))
        assertTrue(EditableFileType.isEditable("Makefile"))
        assertTrue(EditableFileType.isEditable("Dockerfile"))
    }

    @Test
    fun `known dotfiles are editable`() {
        assertTrue(EditableFileType.isEditable(".bashrc"))
        assertTrue(EditableFileType.isEditable(".gitignore"))
    }

    @Test
    fun `unknown dotless names are not editable`() {
        assertFalse(EditableFileType.isEditable("binaryblob"))
        assertFalse(EditableFileType.isEditable("data"))
    }

    @Test
    fun `empty or trailing-dot names do not crash and are not editable`() {
        assertFalse(EditableFileType.isEditable(""))
        assertFalse(EditableFileType.isEditable("   "))
        assertFalse(EditableFileType.isEditable("weird."))
    }
}
