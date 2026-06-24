package com.example.sftping.ui.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sftping.data.connection.ConnectionProfile
import com.example.sftping.data.connection.ConnectionRepository
import com.example.sftping.security.SecretStore
import com.example.sftping.sftp.HostKeyResult
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.SftpException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionUiState(
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val password: String = "",
    val useKeyAuth: Boolean = false,
    val saveCredentials: Boolean = true,
    val connecting: Boolean = false,
    val error: String? = null,
    val hostKeyResult: HostKeyResult? = null,
    val recentConnections: List<ConnectionProfile> = emptyList()
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val sftpClient: ISftpClient,
    private val connectionRepo: ConnectionRepository,
    private val secretStore: SecretStore
) : ViewModel() {

    var uiState by mutableStateOf(ConnectionUiState())
        private set

    private val _navigateToFiles = MutableSharedFlow<Unit>()
    val navigateToFiles: SharedFlow<Unit> = _navigateToFiles

    init {
        viewModelScope.launch { loadRecent() }
    }

    fun updateHost(v: String) { uiState = uiState.copy(host = v, error = null) }
    fun updatePort(v: String) { uiState = uiState.copy(port = v.filter { it.isDigit() }, error = null) }
    fun updateUsername(v: String) { uiState = uiState.copy(username = v, error = null) }
    fun updatePassword(v: String) { uiState = uiState.copy(password = v, error = null) }
    fun toggleAuthMethod() { uiState = uiState.copy(useKeyAuth = !uiState.useKeyAuth) }
    fun toggleSaveCredentials() { uiState = uiState.copy(saveCredentials = !uiState.saveCredentials) }
    fun clearError() { uiState = uiState.copy(error = null) }

    fun connect() {
        val s = uiState
        viewModelScope.launch {
            uiState = s.copy(connecting = true, error = null, hostKeyResult = null)
            try {
                val result = sftpClient.connect(s.host, s.port.toInt(), s.username, s.password)
                uiState = uiState.copy(connecting = false, hostKeyResult = result)
                if (result is HostKeyResult.Trusted) {
                    onConnected()
                }
            } catch (e: SftpException) {
                uiState = uiState.copy(connecting = false, error = e.message ?: "Connection failed")
            } catch (e: Exception) {
                uiState = uiState.copy(
                    connecting = false,
                    error = e.message ?: "Connection failed"
                )
            }
        }
    }

    fun trustAndProceed() {
        val s = uiState
        viewModelScope.launch {
            uiState = s.copy(connecting = true, error = null)
            try {
                sftpClient.trustAndProceed(s.host)
                uiState = uiState.copy(connecting = false, hostKeyResult = null)
                onConnected()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    connecting = false,
                    error = e.message ?: "Trust failed"
                )
            }
        }
    }

    fun rejectKey() {
        viewModelScope.launch {
            sftpClient.disconnect()
            uiState = uiState.copy(hostKeyResult = null)
        }
    }

    private suspend fun onConnected() {
        val profile = ConnectionProfile(
            host = uiState.host,
            port = uiState.port.toIntOrNull() ?: 22,
            username = uiState.username
        )
        connectionRepo.addRecent(profile)
        if (uiState.saveCredentials && uiState.password.isNotEmpty()) {
            secretStore.seal(credentialId(profile), uiState.password)
        }
        _navigateToFiles.emit(Unit)
    }

    fun selectRecent(profile: ConnectionProfile) {
        viewModelScope.launch {
            uiState = uiState.copy(
                host = profile.host,
                port = profile.port.toString(),
                username = profile.username,
                password = "",
                error = null
            )
            secretStore.unseal(credentialId(profile))?.let {
                uiState = uiState.copy(password = it)
            }
        }
    }

    private suspend fun loadRecent() {
        val recents = connectionRepo.loadRecent()
        uiState = uiState.copy(recentConnections = recents)
    }

    private fun credentialId(profile: ConnectionProfile): String =
        "${profile.host}:${profile.port}:${profile.username}"
}
