package com.bm6.monitor.ble

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBleConnection : BleConnection {

    private val _characteristicsFound = MutableStateFlow(false)
    override val characteristicsFound: StateFlow<Boolean> = _characteristicsFound.asStateFlow()

    private val _notificationData = MutableSharedFlow<ByteArray>()
    override val notificationData: SharedFlow<ByteArray> = _notificationData.asSharedFlow()

    var writeResult = true
    var lastWrittenData: ByteArray? = null
        private set

    override suspend fun writeCharacteristic(data: ByteArray): Boolean {
        lastWrittenData = data
        return writeResult
    }

    fun setCharacteristicsFound(found: Boolean) {
        _characteristicsFound.value = found
    }

    suspend fun emitNotification(data: ByteArray) {
        _notificationData.emit(data)
    }
}
