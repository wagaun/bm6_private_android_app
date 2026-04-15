package com.bm6.monitor.ble

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Bm6Protocol {

    private val key = SecretKeySpec(
        byteArrayOf(108, 101, 97, 103, 101, 110, 100, -1, -2, 48, 49, 48, 48, 48, 48, 57),
        "AES",
    )
    private val iv = IvParameterSpec(ByteArray(16))

    private val pollCommand = byteArrayOf(
        0xd1.toByte(), 0x55, 0x07, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
    )

    fun buildPollCommand(): ByteArray {
        return encrypt(pollCommand)
    }

    fun parseResponse(encrypted: ByteArray): Bm6Reading {
        require(encrypted.size >= 16) { "Response must be at least 16 bytes" }

        val decrypted = decrypt(encrypted)

        val tempSign = decrypted[3].toInt() and 0xFF
        val tempValue = decrypted[4].toInt() and 0xFF
        val temperature = if (tempSign == 1) -tempValue else tempValue

        val voltage = ((decrypted[7].toInt() and 0xFF) shl 8) or (decrypted[8].toInt() and 0xFF)

        return Bm6Reading(
            voltageCentivolts = voltage,
            temperatureCelsius = temperature,
            timestamp = System.currentTimeMillis(),
        )
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(data)
    }

    private fun decrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(data)
    }
}
