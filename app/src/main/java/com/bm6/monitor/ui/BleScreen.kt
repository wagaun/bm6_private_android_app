package com.bm6.monitor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bm6.monitor.ble.ConnectionState
import com.bm6.monitor.ble.DiscoveredDevice
import com.bm6.monitor.ble.ReadingState
import com.bm6.monitor.ble.ScanState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BleScreen(
    scanState: ScanState,
    connectionState: ConnectionState,
    devices: List<DiscoveredDevice>,
    characteristicsFound: Boolean,
    readingState: ReadingState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (DiscoveredDevice) -> Unit,
    onDisconnect: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Connection status banner
        ConnectionBanner(connectionState, characteristicsFound, readingState, onDisconnect, onRefresh)

        Spacer(modifier = Modifier.height(16.dp))

        // Scan button
        val isConnected = connectionState == ConnectionState.Connected ||
            connectionState == ConnectionState.Connecting
        when (scanState) {
            ScanState.Scanning -> {
                OutlinedButton(
                    onClick = onStopScan,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Stop Scan")
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            ScanState.Idle -> {
                Button(
                    onClick = onStartScan,
                    enabled = !isConnected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Scan for Devices")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device list
        if (devices.isEmpty() && scanState == ScanState.Idle) {
            Text(
                text = "No devices found. Tap Scan to search.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(devices, key = { it.address }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device) },
                        enabled = !isConnected,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionBanner(
    connectionState: ConnectionState,
    characteristicsFound: Boolean,
    readingState: ReadingState,
    onDisconnect: () -> Unit,
    onRefresh: () -> Unit,
) {
    when (connectionState) {
        ConnectionState.Connecting -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Connecting...",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        ConnectionState.Connected -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(onClick = onDisconnect) {
                            Text("Disconnect")
                        }
                    }

                    if (characteristicsFound) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ReadingContent(readingState, onRefresh)
                    }
                }
            }
        }
        ConnectionState.Error -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Connection error",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        ConnectionState.Disconnected -> { /* No banner */ }
    }
}

@Composable
private fun ReadingContent(
    readingState: ReadingState,
    onRefresh: () -> Unit,
) {
    when (readingState) {
        ReadingState.Idle -> { /* Waiting for first read */ }
        ReadingState.Reading -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reading...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        is ReadingState.Success -> {
            val reading = readingState.reading
            Text(
                text = "Voltage: ${"%.2f".format(reading.voltageVolts)}V",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Temperature: ${reading.temperatureCelsius}°C",
                style = MaterialTheme.typography.titleMedium,
            )
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            Text(
                text = "Last updated: ${timeFormat.format(Date(reading.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onRefresh) {
                Text("Refresh")
            }
        }
        is ReadingState.Error -> {
            Text(
                text = readingState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onRefresh) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: DiscoveredDevice,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val macSuffix = device.address.takeLast(5) // "EE:FF"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = macSuffix,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${device.rssi} dBm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
