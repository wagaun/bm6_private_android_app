package com.bm6.monitor.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.bm6.monitor.ble.BleConnectionManager
import com.bm6.monitor.ble.BleScanner
import com.bm6.monitor.ble.ConnectionState
import com.bm6.monitor.ble.DiscoveredDevice
import com.bm6.monitor.ble.ScanState
import kotlinx.coroutines.flow.StateFlow

class BleViewModel(
    private val scanner: BleScanner = BleScanner(),
    private val connectionManager: BleConnectionManager = BleConnectionManager(),
) : ViewModel() {

    val scanState: StateFlow<ScanState> = scanner.scanState
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = scanner.discoveredDevices
    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState
    val characteristicsFound: StateFlow<Boolean> = connectionManager.characteristicsFound

    val isScanning: Boolean get() = scanner.isScanning
    val isConnected: Boolean get() = connectionManager.isConnected

    fun startScan(context: Context) {
        scanner.startScan(context)
    }

    fun stopScan() {
        scanner.stopScan()
    }

    fun connectToDevice(context: Context, device: DiscoveredDevice) {
        scanner.stopScan()

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter = bluetoothManager?.adapter ?: return
        val bluetoothDevice = adapter.getRemoteDevice(device.address)

        connectionManager.connect(context, bluetoothDevice)
    }

    fun disconnect() {
        connectionManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        scanner.stopScan()
        connectionManager.disconnect()
        connectionManager.close()
    }
}
