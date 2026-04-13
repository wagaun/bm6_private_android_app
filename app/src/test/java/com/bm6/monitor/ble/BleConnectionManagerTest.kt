package com.bm6.monitor.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleConnectionManagerTest {

    private lateinit var manager: BleConnectionManager

    @Before
    fun setup() {
        manager = BleConnectionManager()
    }

    @Test
    fun `initial state is Disconnected`() {
        assertEquals(ConnectionState.Disconnected, manager.connectionState.value)
    }

    @Test
    fun `initial connected device address is null`() {
        assertNull(manager.connectedDeviceAddress)
    }

    @Test
    fun `isConnected is false initially`() {
        assertFalse(manager.isConnected)
    }

    @Test
    fun `setConnectionState updates state flow`() {
        manager.setConnectionState(ConnectionState.Connecting)
        assertEquals(ConnectionState.Connecting, manager.connectionState.value)

        manager.setConnectionState(ConnectionState.Connected)
        assertEquals(ConnectionState.Connected, manager.connectionState.value)
    }

    @Test
    fun `isConnected returns true when Connected`() {
        manager.setConnectionState(ConnectionState.Connected)
        assertTrue(manager.isConnected)
    }

    @Test
    fun `isConnected returns false when Connecting`() {
        manager.setConnectionState(ConnectionState.Connecting)
        assertFalse(manager.isConnected)
    }

    @Test
    fun `characteristicsFound is false initially`() {
        assertFalse(manager.characteristicsFound.value)
    }

    @Test
    fun `setCharacteristicsFound updates state`() {
        manager.setCharacteristicsFound(true)
        assertTrue(manager.characteristicsFound.value)
    }

    @Test
    fun `BM6 characteristic UUIDs are correct`() {
        // FFF3 = write, FFF4 = notify — from the BM6 protocol
        assertTrue(BleConnectionManager.UUID_CHAR_WRITE.toString().contains("fff3"))
        assertTrue(BleConnectionManager.UUID_CHAR_NOTIFY.toString().contains("fff4"))
    }

    @Test
    fun `ConnectionState enum has all expected values`() {
        val states = ConnectionState.entries
        assertEquals(4, states.size)
        assertTrue(states.contains(ConnectionState.Disconnected))
        assertTrue(states.contains(ConnectionState.Connecting))
        assertTrue(states.contains(ConnectionState.Connected))
        assertTrue(states.contains(ConnectionState.Error))
    }
}
