package com.bm6.monitor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionScreen(
    permissionsGranted: Boolean,
    showRationale: Boolean = false,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (permissionsGranted) {
            Text(
                text = "Permissions granted",
                style = MaterialTheme.typography.headlineSmall,
            )
        } else {
            Text(
                text = "Bluetooth Permissions Required",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BM6 Monitor needs Bluetooth permissions to scan for and connect to your battery monitor.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRequestPermissions) {
                Text("Grant BLE Permissions")
            }

            if (showRationale) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            }
        }
    }
}
