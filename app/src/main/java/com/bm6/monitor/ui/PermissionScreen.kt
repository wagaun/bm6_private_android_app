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
import com.bm6.monitor.ble.BleStatus

@Composable
fun PermissionScreen(
    permissionsGranted: Boolean,
    bleStatus: BleStatus = BleStatus.Ready,
    showRationale: Boolean = false,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onEnableBluetooth: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when {
            bleStatus == BleStatus.BleNotSupported -> {
                Text(
                    text = "BLE Not Supported",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This device does not support Bluetooth Low Energy.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            bleStatus == BleStatus.BluetoothDisabled -> {
                Text(
                    text = "Bluetooth Disabled",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Please enable Bluetooth to scan for battery monitors.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onEnableBluetooth) {
                    Text("Enable Bluetooth")
                }
            }

            bleStatus == BleStatus.LocationDisabled -> {
                Text(
                    text = "Location Services Required",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "On this Android version, BLE scanning requires Location Services to be enabled.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            }

            !permissionsGranted -> {
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

            else -> {
                Text(
                    text = "Permissions granted",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}
