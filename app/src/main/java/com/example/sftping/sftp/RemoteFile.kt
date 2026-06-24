package com.example.sftping.sftp

data class RemoteFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean
) {
    val formattedSize: String get() = formatFileSize(size)

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes < 0) return "—"
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
                bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
                else -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
            }
        }
    }
}
