package com.example.sftping.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoStackTest {

    @Test
    fun `initial state has no undo or redo`() {
        val stack = UndoStack("hello")
        assertEquals("hello", stack.current)
        assertFalse(stack.canUndo)
        assertFalse(stack.canRedo)
    }

    @Test
    fun `push then undo restores previous snapshot`() {
        val stack = UndoStack("a")
        stack.push("ab")
        assertEquals("ab", stack.current)
        assertTrue(stack.canUndo)

        assertEquals("a", stack.undo())
        assertTrue(stack.canRedo)
    }

    @Test
    fun `undo then redo returns to pushed snapshot`() {
        val stack = UndoStack("a")
        stack.push("ab")
        stack.undo()
        assertEquals("ab", stack.redo())
        assertFalse(stack.canRedo)
    }

    @Test
    fun `undo at head is a no-op`() {
        val stack = UndoStack("a")
        assertEquals("a", stack.undo())
        assertFalse(stack.canUndo)
    }

    @Test
    fun `redo at tail is a no-op`() {
        val stack = UndoStack("a")
        stack.push("ab")
        assertEquals("ab", stack.redo())
        assertFalse(stack.canRedo)
    }

    @Test
    fun `new push after undo truncates redo branch`() {
        val stack = UndoStack("a")
        stack.push("ab")
        stack.undo()
        stack.push("ac")
        assertEquals("ac", stack.current)
        assertFalse(stack.canRedo)
    }

    @Test
    fun `pushing the current value is ignored`() {
        val stack = UndoStack("a")
        stack.push("a")
        assertFalse(stack.canUndo)
        assertEquals("a", stack.current)
    }

    @Test
    fun `reset collapses history to a single snapshot`() {
        val stack = UndoStack("a")
        stack.push("ab")
        stack.push("abc")
        stack.reset("fresh")
        assertEquals("fresh", stack.current)
        assertFalse(stack.canUndo)
        assertFalse(stack.canRedo)
    }

    @Test
    fun `multiple undos walk back through history`() {
        val stack = UndoStack("a")
        stack.push("ab")
        stack.push("abc")
        assertEquals("ab", stack.undo())
        assertEquals("a", stack.undo())
        assertFalse(stack.canUndo)
    }
}
