package com.example.sftping.sftp

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionState @Inject constructor() {
    @Volatile
    var initialDirectory: String = "/"
}
