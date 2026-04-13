package com.bm6.monitor.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class BleStatusCheckerTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `returns Ready when bluetooth is enabled on API 31+`() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        shadowOf(manager.adapter).setEnabled(true)

        val status = BleStatusChecker.check(context)

        assertEquals(BleStatus.Ready, status)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `returns BluetoothDisabled when adapter is off on API 31+`() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        shadowOf(manager.adapter).setEnabled(false)

        val status = BleStatusChecker.check(context)

        assertEquals(BleStatus.BluetoothDisabled, status)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `returns LocationDisabled when location is off on API 30`() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        shadowOf(manager.adapter).setEnabled(true)

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        shadowOf(locationManager).setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)

        val status = BleStatusChecker.check(context)

        assertEquals(BleStatus.LocationDisabled, status)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `returns Ready when bluetooth and location are enabled on API 30`() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        shadowOf(manager.adapter).setEnabled(true)

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val status = BleStatusChecker.check(context)

        assertEquals(BleStatus.Ready, status)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `returns BleNotSupported when adapter is null`() {
        // Simulate no BLE by getting status with a null adapter
        val status = BleStatusChecker.checkAdapter(null, locationEnabled = true)

        assertEquals(BleStatus.BleNotSupported, status)
    }
}
