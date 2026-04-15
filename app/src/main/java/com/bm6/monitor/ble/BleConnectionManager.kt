package com.bm6.monitor.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Error,
}

class BleConnectionManager : BleConnection {

    companion object {
        // BM6 BLE characteristic UUIDs
        val UUID_CHAR_WRITE: UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb")
        val UUID_CHAR_NOTIFY: UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    }

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _characteristicsFound = MutableStateFlow(false)
    override val characteristicsFound: StateFlow<Boolean> = _characteristicsFound.asStateFlow()

    private val _notificationData = MutableSharedFlow<ByteArray>()
    override val notificationData: SharedFlow<ByteArray> = _notificationData.asSharedFlow()

    val isConnected: Boolean get() = _connectionState.value == ConnectionState.Connected
    var connectedDeviceAddress: String? = null
        private set

    private var gatt: BluetoothGatt? = null
    var writeCharacteristic: BluetoothGattCharacteristic? = null
        private set
    var notifyCharacteristic: BluetoothGattCharacteristic? = null
        private set

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.Connected
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.Disconnected
                    connectedDeviceAddress = null
                    cleanup()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = ConnectionState.Error
                return
            }

            // Search all services for FFF3 and FFF4 characteristics
            for (service in gatt.services) {
                for (characteristic in service.characteristics) {
                    when (characteristic.uuid) {
                        UUID_CHAR_WRITE -> writeCharacteristic = characteristic
                        UUID_CHAR_NOTIFY -> notifyCharacteristic = characteristic
                    }
                }
            }

            _characteristicsFound.value = writeCharacteristic != null && notifyCharacteristic != null
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(context: Context, device: BluetoothDevice) {
        if (isConnected) return

        _connectionState.value = ConnectionState.Connecting
        connectedDeviceAddress = device.address
        gatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun close() {
        gatt?.close()
        cleanup()
    }

    @SuppressLint("MissingPermission")
    override suspend fun writeCharacteristic(data: ByteArray): Boolean {
        val characteristic = writeCharacteristic ?: return false
        val bluetoothGatt = gatt ?: return false
        characteristic.value = data
        return bluetoothGatt.writeCharacteristic(characteristic)
    }

    private fun cleanup() {
        gatt = null
        writeCharacteristic = null
        notifyCharacteristic = null
        _characteristicsFound.value = false
    }

    // Visible for testing
    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }

    // Visible for testing
    fun setCharacteristicsFound(found: Boolean) {
        _characteristicsFound.value = found
    }
}
