package com.example.sftping.ui.editor

/**
 * A pure, JVM-testable undo/redo history of text snapshots.
 *
 * Holds an immutable list of snapshots and a cursor. [push] records a new
 * snapshot (truncating any redo branch); [undo]/[redo] move the cursor. No
 * Android or Compose dependencies so it can be unit-tested directly.
 */
class UndoStack(initial: String = "") {

    private val history = mutableListOf(initial)
    private var cursor = 0

    val current: String
        get() = history[cursor]

    val canUndo: Boolean
        get() = cursor > 0

    val canRedo: Boolean
        get() = cursor < history.size - 1

    /**
     * Record [value] as the new current snapshot. Ignored if it equals the
     * current snapshot. Any snapshots ahead of the cursor (the redo branch) are
     * discarded.
     */
    fun push(value: String) {
        if (value == current) return
        if (canRedo) {
            history.subList(cursor + 1, history.size).clear()
        }
        history.add(value)
        cursor = history.size - 1
    }

    /** Move back one snapshot if possible and return the new [current]. */
    fun undo(): String {
        if (canUndo) cursor--
        return current
    }

    /** Move forward one snapshot if possible and return the new [current]. */
    fun redo(): String {
        if (canRedo) cursor++
        return current
    }

    /** Reset the history to a single [value] snapshot (e.g. on opening a file). */
    fun reset(value: String) {
        history.clear()
        history.add(value)
        cursor = 0
    }
}
