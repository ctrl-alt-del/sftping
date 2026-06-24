package com.example.sftping.ui.connection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sftping.data.connection.ConnectionProfile
import com.example.sftping.sftp.HostKeyResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    onConnected: () -> Unit,
    viewModel: ConnectionViewModel = viewModel()
) {
    val state = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateToFiles.collect { onConnected() }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New connection") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            RecentSection(state.recentConnections, viewModel::selectRecent)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.host,
                onValueChange = viewModel::updateHost,
                label = { Text("Host") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.port,
                    onValueChange = viewModel::updatePort,
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp)
                )
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::updateUsername,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                label = { Text(if (state.useKeyAuth) "Private key path" else "Password") },
                singleLine = true,
                visualTransformation = if (state.useKeyAuth) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Save credentials", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    "(encrypted)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = state.saveCredentials,
                    onCheckedChange = { viewModel.toggleSaveCredentials() }
                )
            }
            Spacer(Modifier.height(20.dp))

            FilledTonalButton(
                onClick = { viewModel.connect() },
                enabled = !state.connecting && state.host.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.connecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Cloud, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Connect")
                }
            }
        }
    }

    HostKeyDialog(
        result = state.hostKeyResult,
        onTrust = viewModel::trustAndProceed,
        onReject = viewModel::rejectKey
    )
}

@Composable
private fun RecentSection(
    recents: List<ConnectionProfile>,
    onSelect: (ConnectionProfile) -> Unit
) {
    if (recents.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recent connections")
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            recents.forEach { profile ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("${profile.username}@${profile.host}:${profile.port}")
                            if (profile.nickname.isNotBlank()) {
                                Text(
                                    profile.nickname,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(profile)
                    }
                )
            }
        }
    }
}

@Composable
private fun HostKeyDialog(
    result: HostKeyResult?,
    onTrust: () -> Unit,
    onReject: () -> Unit
) {
    when (result) {
        is HostKeyResult.Unknown -> AlertDialog(
            icon = { Icon(Icons.Filled.Shield, contentDescription = null) },
            title = { Text("Verify host key") },
            text = {
                Column {
                    Text("First time connecting to ${result.host}. Confirm the server fingerprint:")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        result.fingerprint,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        result.keyType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = { Button(onClick = onTrust) { Text("Trust & connect") } },
            dismissButton = { TextButton(onClick = onReject) { Text("Reject") } },
            onDismissRequest = {}
        )

        is HostKeyResult.Changed -> AlertDialog(
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            title = { Text("Host key changed!") },
            text = {
                Column {
                    Text("The server's host key does not match the previously trusted key. This could be a MITM attack.")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Stored: ${result.storedFingerprint}\nPresented: ${result.presentedFingerprint}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = { TextButton(onClick = onTrust) { Text("Trust anyway") } },
            dismissButton = { Button(onClick = onReject) { Text("Reject") } },
            onDismissRequest = {}
        )
        else -> {}
    }
}
