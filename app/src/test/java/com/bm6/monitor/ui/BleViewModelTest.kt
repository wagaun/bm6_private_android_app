package com.bm6.monitor.ui

import com.bm6.monitor.ble.BleConnectionManager
import com.bm6.monitor.ble.BleScanner
import com.bm6.monitor.ble.ConnectionState
import com.bm6.monitor.ble.DiscoveredDevice
import com.bm6.monitor.ble.ScanState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleViewModelTest {

    private lateinit var scanner: BleScanner
    private lateinit var connectionManager: BleConnectionManager
    private lateinit var viewModel: BleViewModel

    @Before
    fun setup() {
        scanner = BleScanner()
        connectionManager = BleConnectionManager()
        viewModel = BleViewModel(scanner, connectionManager)
    }

    @Test
    fun `initial scan state is Idle`() {
        assertEquals(ScanState.Idle, viewModel.scanState.value)
    }

    @Test
    fun `initial connection state is Disconnected`() {
        assertEquals(ConnectionState.Disconnected, viewModel.connectionState.value)
    }

    @Test
    fun `initial devices list is empty`() {
        assertTrue(viewModel.discoveredDevices.value.isEmpty())
    }

    @Test
    fun `scanner state flows through to viewmodel`() {
        scanner.setScanState(ScanState.Scanning)
        assertEquals(ScanState.Scanning, viewModel.scanState.value)
    }

    @Test
    fun `connection state flows through to viewmodel`() {
        connectionManager.setConnectionState(ConnectionState.Connecting)
        assertEquals(ConnectionState.Connecting, viewModel.connectionState.value)
    }

    @Test
    fun `discovered devices flow through to viewmodel`() {
        val device = DiscoveredDevice(name = "BM6", address = "AA:BB:CC:DD:EE:FF", rssi = -65)
        scanner.addDiscoveredDevice(device)

        assertEquals(1, viewModel.discoveredDevices.value.size)
        assertEquals(device, viewModel.discoveredDevices.value[0])
    }

    @Test
    fun `characteristicsFound flows through to viewmodel`() {
        assertFalse(viewModel.characteristicsFound.value)

        connectionManager.setCharacteristicsFound(true)
        assertTrue(viewModel.characteristicsFound.value)
    }

    @Test
    fun `isScanning delegates to scanner`() {
        assertFalse(viewModel.isScanning)

        scanner.setScanState(ScanState.Scanning)
        assertTrue(viewModel.isScanning)
    }

    @Test
    fun `isConnected delegates to connection manager`() {
        assertFalse(viewModel.isConnected)

        connectionManager.setConnectionState(ConnectionState.Connected)
        assertTrue(viewModel.isConnected)
    }
}
