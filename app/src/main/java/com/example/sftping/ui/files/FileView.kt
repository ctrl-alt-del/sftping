package com.example.sftping.ui.files

import com.example.sftping.sftp.RemoteFile

enum class SortMode { NAME_ASC, NAME_DESC, SIZE, LAST_MODIFIED }

object FileView {
    fun apply(
        files: List<RemoteFile>,
        showHidden: Boolean,
        query: String,
        sortMode: SortMode
    ): List<RemoteFile> {
        val trimmed = query.trim()
        val filtered = files.filter { file ->
            (showHidden || !file.name.startsWith(".")) &&
                (trimmed.isEmpty() || file.name.contains(trimmed, ignoreCase = true))
        }
        val comparator = compareByDescending<RemoteFile> { it.isDirectory }
            .then(sortComparator(sortMode))
            .thenBy { it.name.lowercase() }
        return filtered.sortedWith(comparator)
    }

    private fun sortComparator(mode: SortMode): Comparator<RemoteFile> = when (mode) {
        SortMode.NAME_ASC -> compareBy { it.name.lowercase() }
        SortMode.NAME_DESC -> compareByDescending { it.name.lowercase() }
        SortMode.SIZE -> compareByDescending { it.size }
        SortMode.LAST_MODIFIED -> compareByDescending { it.lastModified }
    }
}
