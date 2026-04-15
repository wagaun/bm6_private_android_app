package com.bm6.monitor.ble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

class Bm6DataReader(
    private val connection: BleConnection,
    private val protocol: Bm6Protocol,
    private val timeoutMillis: Long = 5000L,
) {

    private val _readingState = MutableStateFlow<ReadingState>(ReadingState.Idle)
    val readingState: StateFlow<ReadingState> = _readingState.asStateFlow()

    suspend fun requestReading() {
        _readingState.value = ReadingState.Reading

        val command = protocol.buildPollCommand()
        val written = connection.writeCharacteristic(command)
        if (!written) {
            _readingState.value = ReadingState.Error("Write failed")
            return
        }

        val responseBytes = try {
            withTimeout(timeoutMillis) {
                connection.notificationData.first()
            }
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            _readingState.value = ReadingState.Error("Device did not respond")
            return
        }

        val reading = try {
            protocol.parseResponse(responseBytes)
        } catch (_: Exception) {
            _readingState.value = ReadingState.Error("Invalid response")
            return
        }

        _readingState.value = ReadingState.Success(reading)
    }

    fun resetState() {
        _readingState.value = ReadingState.Idle
    }
}
