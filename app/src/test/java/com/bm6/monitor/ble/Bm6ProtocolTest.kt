package com.bm6.monitor.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Bm6ProtocolTest {

    private val protocol = Bm6Protocol()

    // Known test vector from BM6 protocol documentation
    private val pollPlaintext = hexToBytes("d1550700000000000000000000000000")
    private val pollEncrypted = hexToBytes("697ea0b5d54cf024e794772355554114")

    // Known response: voltage=11.96V (1196 centivolts), temperature=24°C (positive)
    private val responsePlaintext = hexToBytes("d155070018010004ac00000000020000")

    @Test
    fun `buildPollCommand encrypts to known test vector`() {
        val result = protocol.buildPollCommand()
        assertEquals(pollEncrypted.toHex(), result.toHex())
    }

    @Test
    fun `parseResponse decrypts and parses voltage correctly`() {
        val encrypted = encryptForTest(responsePlaintext)
        val reading = protocol.parseResponse(encrypted)
        assertEquals(1196, reading.voltageCentivolts)
    }

    @Test
    fun `parseResponse decrypts and parses positive temperature`() {
        val encrypted = encryptForTest(responsePlaintext)
        val reading = protocol.parseResponse(encrypted)
        assertEquals(24, reading.temperatureCelsius)
    }

    @Test
    fun `parseResponse handles negative temperature`() {
        // Modify response: sign byte (byte 3) = 0x01, temp (byte 4) = 0x05 → -5°C
        val negTempPlaintext = responsePlaintext.copyOf()
        negTempPlaintext[3] = 0x01
        negTempPlaintext[4] = 0x05

        val encrypted = encryptForTest(negTempPlaintext)
        val reading = protocol.parseResponse(encrypted)
        assertEquals(-5, reading.temperatureCelsius)
    }

    @Test
    fun `parseResponse sets timestamp`() {
        val encrypted = encryptForTest(responsePlaintext)
        val before = System.currentTimeMillis()
        val reading = protocol.parseResponse(encrypted)
        val after = System.currentTimeMillis()
        assert(reading.timestamp in before..after)
    }

    @Test
    fun `parseResponse throws on input shorter than 16 bytes`() {
        val shortInput = ByteArray(8)
        assertThrows(IllegalArgumentException::class.java) {
            protocol.parseResponse(shortInput)
        }
    }

    @Test
    fun `Bm6Reading voltageVolts computes correctly`() {
        val reading = Bm6Reading(
            voltageCentivolts = 1196,
            temperatureCelsius = 24,
            timestamp = 0L,
        )
        assertEquals(11.96, reading.voltageVolts, 0.001)
    }

    @Test
    fun `Bm6Reading voltageVolts handles zero`() {
        val reading = Bm6Reading(
            voltageCentivolts = 0,
            temperatureCelsius = 0,
            timestamp = 0L,
        )
        assertEquals(0.0, reading.voltageVolts, 0.001)
    }

    // --- helpers ---

    private fun encryptForTest(plaintext: ByteArray): ByteArray {
        val key = byteArrayOf(108, 101, 97, 103, 101, 110, 100, -1, -2, 48, 49, 48, 48, 48, 48, 57)
        val iv = ByteArray(16)
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(plaintext)
    }

    private fun hexToBytes(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
