package com.bm6.monitor.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bm6.monitor.ui.theme.BM6MonitorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PermissionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows grant permission button when permissions not granted`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                PermissionScreen(
                    permissionsGranted = false,
                    onRequestPermissions = {},
                    onOpenSettings = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Grant BLE Permissions").assertIsDisplayed()
    }

    @Test
    fun `shows explanation text when permissions not granted`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                PermissionScreen(
                    permissionsGranted = false,
                    onRequestPermissions = {},
                    onOpenSettings = {},
                )
            }
        }

        composeTestRule.onNodeWithText("BM6 Monitor needs Bluetooth permissions to scan for and connect to your battery monitor.", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows permissions granted message when permissions are granted`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                PermissionScreen(
                    permissionsGranted = true,
                    onRequestPermissions = {},
                    onOpenSettings = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Permissions granted", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows open settings button when permissions are denied`() {
        composeTestRule.setContent {
            BM6MonitorTheme {
                PermissionScreen(
                    permissionsGranted = false,
                    showRationale = true,
                    onRequestPermissions = {},
                    onOpenSettings = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Open Settings").assertIsDisplayed()
    }
}
