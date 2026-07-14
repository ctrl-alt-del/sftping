package com.example.sftping.ui.files

/**
 * Pure allowlist deciding whether a remote file is openable in the text Editor.
 * SFTP write access can't be predicted from a listing, so this gates only on
 * *type* (extension / known filename), never on permissions.
 */
object EditableFileType {

    private val editableExtensions = setOf(
        "txt", "md", "markdown", "log", "csv", "tsv",
        "json", "xml", "yml", "yaml", "toml", "ini", "conf", "cfg", "properties", "env",
        "sh", "bash", "zsh", "kt", "kts", "java", "py", "rb", "go", "rs", "c", "h",
        "cpp", "hpp", "js", "ts", "jsx", "tsx", "css", "scss", "html", "htm",
        "gradle", "sql", "gitignore"
    )

    private val editableFilenames = setOf(
        "readme", "license", "makefile", "dockerfile", "gemfile", "procfile",
        ".bashrc", ".bash_profile", ".zshrc", ".profile", ".gitignore",
        ".gitconfig", ".env", ".editorconfig"
    )

    /** True when [name] is a recognized text-editable file. */
    fun isEditable(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val lower = trimmed.lowercase()
        if (lower in editableFilenames) return true
        val ext = lower.substringAfterLast('.', "")
        if (ext.isEmpty()) return false
        return ext in editableExtensions
    }
}
