package com.bm6.monitor

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bm6.monitor.ble.BlePermissionHelper
import com.bm6.monitor.ble.BleStatus
import com.bm6.monitor.ble.BleStatusChecker
import com.bm6.monitor.ui.BleScreen
import com.bm6.monitor.ui.BleViewModel
import com.bm6.monitor.ui.PermissionScreen
import com.bm6.monitor.ui.theme.BM6MonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BM6MonitorTheme {
                var permissionsGranted by remember {
                    mutableStateOf(BlePermissionHelper.hasPermissions(this))
                }
                var bleStatus by remember {
                    mutableStateOf(BleStatusChecker.check(this))
                }
                var showRationale by remember { mutableStateOf(false) }

                // Re-check status when returning from settings or enabling bluetooth
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    permissionsGranted = BlePermissionHelper.hasPermissions(this@MainActivity)
                    bleStatus = BleStatusChecker.check(this@MainActivity)
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    permissionsGranted = results.values.all { it }
                    if (!permissionsGranted) {
                        showRationale = true
                    }
                    bleStatus = BleStatusChecker.check(this)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (permissionsGranted && bleStatus == BleStatus.Ready) {
                        val bleViewModel: BleViewModel = viewModel()
                        val scanState by bleViewModel.scanState.collectAsState()
                        val devices by bleViewModel.discoveredDevices.collectAsState()
                        val connectionState by bleViewModel.connectionState.collectAsState()
                        val charsFound by bleViewModel.characteristicsFound.collectAsState()

                        BleScreen(
                            scanState = scanState,
                            connectionState = connectionState,
                            devices = devices,
                            characteristicsFound = charsFound,
                            onStartScan = { bleViewModel.startScan(this@MainActivity) },
                            onStopScan = { bleViewModel.stopScan() },
                            onDeviceClick = { bleViewModel.connectToDevice(this@MainActivity, it) },
                            onDisconnect = { bleViewModel.disconnect() },
                            modifier = Modifier.padding(innerPadding),
                        )
                    } else {
                        PermissionScreen(
                            permissionsGranted = permissionsGranted,
                            bleStatus = bleStatus,
                            showRationale = showRationale,
                            onRequestPermissions = {
                                permissionLauncher.launch(
                                    BlePermissionHelper.getRequiredPermissions()
                                )
                            },
                            onOpenSettings = {
                                startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null),
                                    )
                                )
                            },
                            onEnableBluetooth = {
                                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            },
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}
