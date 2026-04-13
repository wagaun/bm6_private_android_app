package com.bm6.monitor.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bm6.monitor.ble.ConnectionState
import com.bm6.monitor.ble.DiscoveredDevice
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

    @Test
    fun `shows Scan button when idle`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Disconnected,
                    devices = emptyList(),
                    characteristicsFound = false,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Scan for Devices").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan for Devices").assertIsEnabled()
    }

    @Test
    fun `shows Stop Scan button when scanning`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Scanning,
                    connectionState = ConnectionState.Disconnected,
                    devices = emptyList(),
                    characteristicsFound = false,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Stop Scan").assertIsDisplayed()
    }

    @Test
    fun `shows no devices found when scan idle with empty list`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Disconnected,
                    devices = emptyList(),
                    characteristicsFound = false,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No devices found", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows device in list with name and MAC suffix`() {
        val devices = listOf(
            DiscoveredDevice(name = "BM6", address = "AA:BB:CC:DD:EE:FF", rssi = -65),
        )

        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Disconnected,
                    devices = devices,
                    characteristicsFound = false,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("BM6").assertIsDisplayed()
        composeTestRule.onNodeWithText("EE:FF", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows Unknown for device with null name`() {
        val devices = listOf(
            DiscoveredDevice(name = null, address = "AA:BB:CC:DD:EE:FF", rssi = -65),
        )

        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Disconnected,
                    devices = devices,
                    characteristicsFound = false,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed()
    }

    @Test
    fun `shows connecting status`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Connecting,
                    devices = emptyList(),
                    characteristicsFound = false,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Connecting", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows connected status with disconnect button`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Connected,
                    devices = emptyList(),
                    characteristicsFound = true,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Connected", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
    }

    @Test
    fun `scan button disabled while connected`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                BleScreen(
                    scanState = ScanState.Idle,
                    connectionState = ConnectionState.Connected,
                    devices = emptyList(),
                    characteristicsFound = true,
                    onStartScan = {},
                    onStopScan = {},
                    onDeviceClick = {},
                    onDisconnect = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Scan for Devices").assertIsNotEnabled()
    }
}
