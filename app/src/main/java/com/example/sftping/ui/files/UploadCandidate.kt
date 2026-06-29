package com.example.sftping.ui.files

import android.net.Uri

/** A file the user picked to upload, plus whether it was already uploaded here. */
data class UploadCandidate(
    val uri: Uri,
    val name: String,
    val size: Long,
    val alreadyUploaded: Boolean,
    val selected: Boolean = true
)

/** Pure: the remote destination path for [name] under [currentPath]. */
fun uploadRemotePath(currentPath: String, name: String): String =
    if (currentPath == "/") "/$name" else "$currentPath/$name"

/** Pure: for each name, whether it was already uploaded into [currentPath]. */
fun markUploaded(
    names: List<String>,
    currentPath: String,
    uploadedPaths: Set<String>
): List<Boolean> = names.map { uploadRemotePath(currentPath, it) in uploadedPaths }
