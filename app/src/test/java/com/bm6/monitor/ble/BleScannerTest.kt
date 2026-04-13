package com.bm6.monitor.ble

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
class BleScannerTest {

    private lateinit var scanner: BleScanner

    @Before
    fun setup() {
        scanner = BleScanner()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(ScanState.Idle, scanner.scanState.value)
    }

    @Test
    fun `initial devices list is empty`() {
        assertTrue(scanner.discoveredDevices.value.isEmpty())
    }

    @Test
    fun `addDiscoveredDevice adds a device`() {
        val device = DiscoveredDevice(
            name = "BM6",
            address = "AA:BB:CC:DD:EE:FF",
            rssi = -65,
        )

        scanner.addDiscoveredDevice(device)

        assertEquals(1, scanner.discoveredDevices.value.size)
        assertEquals(device, scanner.discoveredDevices.value[0])
    }

    @Test
    fun `addDiscoveredDevice deduplicates by address and updates rssi`() {
        val device1 = DiscoveredDevice(
            name = "BM6",
            address = "AA:BB:CC:DD:EE:FF",
            rssi = -65,
        )
        val device2 = DiscoveredDevice(
            name = "BM6",
            address = "AA:BB:CC:DD:EE:FF",
            rssi = -50,
        )

        scanner.addDiscoveredDevice(device1)
        scanner.addDiscoveredDevice(device2)

        assertEquals(1, scanner.discoveredDevices.value.size)
        assertEquals(-50, scanner.discoveredDevices.value[0].rssi)
    }

    @Test
    fun `addDiscoveredDevice keeps multiple different devices`() {
        val device1 = DiscoveredDevice(name = "BM6", address = "AA:BB:CC:DD:EE:01", rssi = -65)
        val device2 = DiscoveredDevice(name = "BM6", address = "AA:BB:CC:DD:EE:02", rssi = -70)

        scanner.addDiscoveredDevice(device1)
        scanner.addDiscoveredDevice(device2)

        assertEquals(2, scanner.discoveredDevices.value.size)
    }

    @Test
    fun `clearDevices empties the list`() {
        scanner.addDiscoveredDevice(
            DiscoveredDevice(name = "BM6", address = "AA:BB:CC:DD:EE:FF", rssi = -65)
        )

        scanner.clearDevices()

        assertTrue(scanner.discoveredDevices.value.isEmpty())
    }

    @Test
    fun `isScanning reflects scan state`() {
        assertFalse(scanner.isScanning)

        scanner.setScanState(ScanState.Scanning)
        assertTrue(scanner.isScanning)

        scanner.setScanState(ScanState.Idle)
        assertFalse(scanner.isScanning)
    }
}
