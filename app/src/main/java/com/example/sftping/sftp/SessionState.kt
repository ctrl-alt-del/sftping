package com.example.sftping.sftp

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionState @Inject constructor() {
    @Volatile
    var initialDirectory: String = "/"

    // Incremented on each successful connect so screens can detect a new session.
    @Volatile
    var epoch: Int = 0
}
