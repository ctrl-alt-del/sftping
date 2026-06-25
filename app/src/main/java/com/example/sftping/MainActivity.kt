package com.example.sftping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.sftping.ui.connection.ConnectionScreen
import com.example.sftping.ui.files.FilesScreen
import com.example.sftping.ui.theme.SftpingTheme
import com.example.sftping.ui.transfers.TransfersScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SftpingTheme {
                SftpingShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun SftpingShell() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.CONNECT) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.CONNECT -> ConnectionScreen(
                onConnected = { currentDestination = AppDestinations.FILES }
            )
            AppDestinations.FILES -> FilesScreen(
                onNavigateToConnection = { currentDestination = AppDestinations.CONNECT }
            )
            AppDestinations.TRANSFERS -> TransfersScreen()
        }
    }
}

@Composable
private fun Placeholder(label: String) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class AppDestinations(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    CONNECT("Connect", Icons.Filled.Cloud),
    FILES("Files", Icons.Filled.Folder),
    TRANSFERS("Transfers", Icons.Filled.SwapVert),
}
