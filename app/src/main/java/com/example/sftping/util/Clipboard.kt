package com.example.sftping.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thin abstraction over the system clipboard so copy logic stays JVM-unit-testable. */
interface Clipboard {
    fun copy(label: String, text: String)
}

@Singleton
class AndroidClipboard @Inject constructor(
    @ApplicationContext private val context: Context
) : Clipboard {
    override fun copy(label: String, text: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}

/** In-memory test double mirroring the `KnownHostsStore` / `InMemory*` pattern. */
@Singleton
class InMemoryClipboard @Inject constructor() : Clipboard {
    var lastLabel: String? = null
        private set
    var lastText: String? = null
        private set

    override fun copy(label: String, text: String) {
        lastLabel = label
        lastText = text
    }
}
