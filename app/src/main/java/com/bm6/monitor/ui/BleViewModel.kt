package com.bm6.monitor.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm6.monitor.ble.Bm6DataReader
import com.bm6.monitor.ble.Bm6Protocol
import com.bm6.monitor.ble.BleConnectionManager
import com.bm6.monitor.ble.BleScanner
import com.bm6.monitor.ble.ConnectionState
import com.bm6.monitor.ble.DiscoveredDevice
import com.bm6.monitor.ble.ReadingState
import com.bm6.monitor.ble.ScanState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BleViewModel(
    private val scanner: BleScanner = BleScanner(),
    private val connectionManager: BleConnectionManager = BleConnectionManager(),
    private val dataReader: Bm6DataReader = Bm6DataReader(connectionManager, Bm6Protocol()),
) : ViewModel() {

    val scanState: StateFlow<ScanState> = scanner.scanState
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = scanner.discoveredDevices
    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState
    val characteristicsFound: StateFlow<Boolean> = connectionManager.characteristicsFound
    val readingState: StateFlow<ReadingState> = dataReader.readingState

    val isScanning: Boolean get() = scanner.isScanning
    val isConnected: Boolean get() = connectionManager.isConnected

    init {
        viewModelScope.launch {
            connectionManager.characteristicsFound.collect { found ->
                if (found) {
                    dataReader.requestReading()
                }
            }
        }

        viewModelScope.launch {
            connectionManager.connectionState.collect { state ->
                if (state == ConnectionState.Disconnected) {
                    dataReader.resetState()
                }
            }
        }
    }

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

    fun refreshReading() {
        viewModelScope.launch {
            dataReader.requestReading()
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanner.stopScan()
        connectionManager.disconnect()
        connectionManager.close()
    }
}
