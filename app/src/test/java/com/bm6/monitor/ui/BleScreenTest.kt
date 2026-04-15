package com.bm6.monitor.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bm6.monitor.ble.Bm6Reading
import com.bm6.monitor.ble.ConnectionState
import com.bm6.monitor.ble.DiscoveredDevice
import com.bm6.monitor.ble.ReadingState
import com.bm6.monitor.ble.ScanState
import com.bm6.monitor.ui.theme.BM6MonitorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setScreen(
        scanState: ScanState = ScanState.Idle,
        connectionState: ConnectionState = ConnectionState.Disconnected,
        devices: List<DiscoveredDevice> = emptyList(),
        characteristicsFound: Boolean = false,
        readingState: ReadingState = ReadingState.Idle,
        onStartScan: () -> Unit = {},
        onStopScan: () -> Unit = {},
        onDeviceClick: (DiscoveredDevice) -> Unit = {},
        onDisconnect: () -> Unit = {},
        onRefresh: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = scanState,
                    connectionState = connectionState,
                    devices = devices,
                    characteristicsFound = characteristicsFound,
                    readingState = readingState,
                    onStartScan = onStartScan,
                    onStopScan = onStopScan,
                    onDeviceClick = onDeviceClick,
                    onDisconnect = onDisconnect,
                    onRefresh = onRefresh,
                )
            }
        }
    }

    @Test
    fun `shows Scan button when idle`() {
        setScreen()
        composeTestRule.onNodeWithText("Scan for Devices").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan for Devices").assertIsEnabled()
    }

    @Test
    fun `shows Stop Scan button when scanning`() {
        setScreen(scanState = ScanState.Scanning)
        composeTestRule.onNodeWithText("Stop Scan").assertIsDisplayed()
    }

    @Test
    fun `shows no devices found when scan idle with empty list`() {
        setScreen()
        composeTestRule.onNodeWithText("No devices found", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows device in list with name and MAC suffix`() {
        val devices = listOf(
            DiscoveredDevice(name = "BM6", address = "AA:BB:CC:DD:EE:FF", rssi = -65),
        )
        setScreen(devices = devices)
        composeTestRule.onNodeWithText("BM6").assertIsDisplayed()
        composeTestRule.onNodeWithText("EE:FF", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows Unknown for device with null name`() {
        val devices = listOf(
            DiscoveredDevice(name = null, address = "AA:BB:CC:DD:EE:FF", rssi = -65),
        )
        setScreen(devices = devices)
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed()
    }

    @Test
    fun `shows connecting status`() {
        setScreen(connectionState = ConnectionState.Connecting)
        composeTestRule.onNodeWithText("Connecting", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows connected status with disconnect button`() {
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
        )
        composeTestRule.onNodeWithText("Connected", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
    }

    @Test
    fun `scan button disabled while connected`() {
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
        )
        composeTestRule.onNodeWithText("Scan for Devices").assertIsNotEnabled()
    }

    // Phase 2: Reading state tests

    @Test
    fun `shows voltage and temperature on successful reading`() {
        val reading = Bm6Reading(
            voltageCentivolts = 1245,
            temperatureCelsius = 24,
            timestamp = System.currentTimeMillis(),
        )
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
            readingState = ReadingState.Success(reading),
        )
        composeTestRule.onNodeWithText("12.45V", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("24°C", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows negative temperature correctly`() {
        val reading = Bm6Reading(
            voltageCentivolts = 1196,
            temperatureCelsius = -5,
            timestamp = System.currentTimeMillis(),
        )
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
            readingState = ReadingState.Success(reading),
        )
        composeTestRule.onNodeWithText("-5°C", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows Refresh button on successful reading`() {
        val reading = Bm6Reading(
            voltageCentivolts = 1196,
            temperatureCelsius = 24,
            timestamp = System.currentTimeMillis(),
        )
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
            readingState = ReadingState.Success(reading),
        )
        composeTestRule.onNodeWithText("Refresh").assertIsDisplayed()
    }

    @Test
    fun `shows reading indicator when reading in progress`() {
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
            readingState = ReadingState.Reading,
        )
        composeTestRule.onNodeWithText("Reading", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows error message on read failure`() {
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
            readingState = ReadingState.Error("Device did not respond"),
        )
        composeTestRule.onNodeWithText("Device did not respond", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows Retry button on read error`() {
        setScreen(
            connectionState = ConnectionState.Connected,
            characteristicsFound = true,
            readingState = ReadingState.Error("Write failed"),
        )
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
