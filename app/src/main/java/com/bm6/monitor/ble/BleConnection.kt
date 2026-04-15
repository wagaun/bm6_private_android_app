package com.bm6.monitor.ble

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BleConnection {
    val characteristicsFound: StateFlow<Boolean>
    val notificationData: SharedFlow<ByteArray>
    suspend fun writeCharacteristic(data: ByteArray): Boolean
}
