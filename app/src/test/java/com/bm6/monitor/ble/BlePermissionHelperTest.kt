package com.bm6.monitor.ble

import android.Manifest
import android.os.Build
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class BlePermissionHelperTest {

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `getRequiredPermissions returns BLUETOOTH_SCAN and BLUETOOTH_CONNECT on API 31+`() {
        val permissions = BlePermissionHelper.getRequiredPermissions()

        assertArrayEquals(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            ),
            permissions,
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `getRequiredPermissions returns legacy permissions on API 30`() {
        val permissions = BlePermissionHelper.getRequiredPermissions()

        assertArrayEquals(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            permissions,
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `getRequiredPermissions returns legacy permissions on API 26`() {
        val permissions = BlePermissionHelper.getRequiredPermissions()

        assertArrayEquals(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            permissions,
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `hasPermissions returns false when permissions are not granted`() {
        val context = RuntimeEnvironment.getApplication()

        assertFalse(BlePermissionHelper.hasPermissions(context))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `getMissingPermissions returns all permissions when none are granted`() {
        val context = RuntimeEnvironment.getApplication()
        val missing = BlePermissionHelper.getMissingPermissions(context)

        assertArrayEquals(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            ),
            missing,
        )
    }
}
