package com.bm6.monitor.ble

data class Bm6Reading(
    val voltageCentivolts: Int,
    val temperatureCelsius: Int,
    val timestamp: Long,
) {
    val voltageVolts: Double get() = voltageCentivolts / 100.0
}

sealed interface ReadingState {
    data object Idle : ReadingState
    data object Reading : ReadingState
    data class Success(val reading: Bm6Reading) : ReadingState
    data class Error(val message: String) : ReadingState
}
