package com.bm6.monitor

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bm6.monitor.ble.BlePermissionHelper
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
                var showRationale by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    permissionsGranted = results.values.all { it }
                    if (!permissionsGranted) {
                        showRationale = true
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionScreen(
                        permissionsGranted = permissionsGranted,
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
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
