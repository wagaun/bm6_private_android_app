package com.bm6.monitor.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Build

enum class BleStatus {
    Ready,
    BleNotSupported,
    BluetoothDisabled,
    LocationDisabled,
}

object BleStatusChecker {

    fun check(context: Context): BleStatus {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter
        val locationEnabled = isLocationEnabled(context)
        return checkAdapter(adapter, locationEnabled)
    }

    fun checkAdapter(adapter: BluetoothAdapter?, locationEnabled: Boolean): BleStatus {
        if (adapter == null) return BleStatus.BleNotSupported
        if (!adapter.isEnabled) return BleStatus.BluetoothDisabled
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !locationEnabled) {
            return BleStatus.LocationDisabled
        }
        return BleStatus.Ready
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
