package com.bm6.monitor.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiscoveredDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
)

enum class ScanState {
    Idle,
    Scanning,
}

class BleScanner {

    companion object {
        const val SCAN_TIMEOUT_MS = 10_000L
    }

    private val _scanState = MutableStateFlow(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    val isScanning: Boolean get() = _scanState.value == ScanState.Scanning

    private var bleScanner: BluetoothLeScanner? = null
    private val handler = Handler(Looper.getMainLooper())

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = DiscoveredDevice(
                name = result.device.name,
                address = result.device.address,
                rssi = result.rssi,
            )
            addDiscoveredDevice(device)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan(context: Context) {
        if (isScanning) return

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bleScanner = bluetoothManager?.adapter?.bluetoothLeScanner ?: return

        clearDevices()
        _scanState.value = ScanState.Scanning

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner?.startScan(null, settings, scanCallback)

        handler.postDelayed({ stopScan() }, SCAN_TIMEOUT_MS)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return

        bleScanner?.stopScan(scanCallback)
        bleScanner = null
        _scanState.value = ScanState.Idle
        handler.removeCallbacksAndMessages(null)
    }

    fun addDiscoveredDevice(device: DiscoveredDevice) {
        val current = _discoveredDevices.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.address == device.address }
        if (existingIndex >= 0) {
            current[existingIndex] = device
        } else {
            current.add(device)
        }
        _discoveredDevices.value = current
    }

    fun clearDevices() {
        _discoveredDevices.value = emptyList()
    }

    fun setScanState(state: ScanState) {
        _scanState.value = state
    }
}
