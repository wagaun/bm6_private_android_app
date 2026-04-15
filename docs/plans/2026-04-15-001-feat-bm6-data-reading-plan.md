---
title: "feat: BM6 Data Reading - Encrypted BLE Protocol Communication"
type: feat
status: completed
date: 2026-04-15
---

# feat: BM6 Data Reading - Encrypted BLE Protocol Communication

## Overview

Implement the BM6 BLE protocol to read battery voltage and temperature from a connected BM6 device. This is Phase 2 of the project — it builds on the BLE scan-and-connect flow from Phase 1 and adds encrypted command/response communication over the FFF3 (write) and FFF4 (notify) characteristics.

The BM6 protocol uses AES-128-CBC encryption with a hardcoded key. The app sends an encrypted poll command to FFF3, receives an encrypted response via FFF4 notification, then decrypts and parses voltage and temperature from the response bytes.

Protocol reference: https://www.tarball.ca/posts/reverse-engineering-the-bm6-ble-battery-monitor/

## Problem Statement / Motivation

Phase 1 established a working BLE connection with characteristic discovery. The app can confirm FFF3/FFF4 exist but cannot yet communicate with the device. Phase 2 closes this gap — after completion, the user connects and immediately sees their battery voltage and temperature.

## Proposed Solution

Add three new components and extend two existing ones:

1. **`Bm6Protocol`** — Pure logic class handling AES-CBC encryption/decryption, command construction, and response parsing
2. **`Bm6Reading`** — Data class representing a parsed voltage/temperature reading
3. **`Bm6DataReader`** — Orchestrator that coordinates the write-command → receive-notification → parse cycle
4. **`BleConnectionManager` extensions** — Notification enablement, write method, notification data flow
5. **UI extensions** — Minimal data display with refresh capability

### Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Protocol + crypto separation | Single `Bm6Protocol` class | Encryption exists solely to serve this protocol; separating would be premature abstraction |
| Crypto library | `javax.crypto.Cipher` (built-in) | AES-CBC with fixed key/IV is a one-liner; no external dependency needed |
| AES padding mode | `NoPadding` | Command and response are exactly 16 bytes (one AES block) |
| Data reading orchestration | New `Bm6DataReader` class | Keeps `BleConnectionManager` generic and `BleViewModel` thin; mirrors existing Scanner/ConnectionManager pattern |
| Read trigger | Auto-read on connect + manual refresh | User sees data immediately; no polling loop complexity |
| Notification setup | Enabled in `onServicesDiscovered` | `characteristicsFound = true` means fully ready to communicate |
| Notification data transport | `SharedFlow<ByteArray>` on `BleConnectionManager` | Consistent with existing StateFlow patterns; decouples GATT callback from consumers |
| Response timeout | 5 seconds | Generous for BLE; short enough for good UX |
| Testability | Extract interface from `BleConnectionManager` | Enables fake-based testing for `Bm6DataReader` without GATT mocking |
| UI scope | Minimal text display | Dashboard polish is Phase 3; Phase 2 proves the protocol works end-to-end |

## Technical Details

### BM6 Protocol Specification

**AES-128-CBC Encryption:**
- Key: `[108, 101, 97, 103, 101, 110, 100, 255, 254, 48, 49, 48, 48, 48, 48, 57]`
- IV: 16 zero bytes
- Mode: CBC, NoPadding

**Poll Command:**
- Plaintext: `d1550700000000000000000000000000` (16 bytes)
- Encrypted: `697ea0b5d54cf024e794772355554114` (16 bytes)

**Response Parsing (decrypted bytes):**
- Bytes 15-17 (3 hex chars → int): Voltage in centivolts. Example: `0x04AC` = 1196 → 11.96V
- Byte 6: Temperature sign (`0x01` = negative, else positive)
- Byte 8: Temperature value in Celsius

**Communication Flow:**
```
Enable FFF4 notifications (CCCD descriptor write)
  → Write encrypted poll command to FFF3
  → Receive encrypted notification on FFF4
  → Decrypt with AES-CBC
  → Parse voltage and temperature from decrypted bytes
```

### Data Model

```kotlin
data class Bm6Reading(
    val voltageCentivolts: Int,    // 1196 = 11.96V
    val temperatureCelsius: Int,   // signed: 24 or -5
    val timestamp: Long,           // System.currentTimeMillis()
) {
    val voltageVolts: Double get() = voltageCentivolts / 100.0
}

sealed interface ReadingState {
    data object Idle : ReadingState
    data object Reading : ReadingState
    data class Success(val reading: Bm6Reading) : ReadingState
    data class Error(val message: String) : ReadingState
}
```

### Interface Extraction

Extract a minimal interface from `BleConnectionManager` for testability:

```kotlin
interface BleConnection {
    val characteristicsFound: StateFlow<Boolean>
    val notificationData: SharedFlow<ByteArray>
    suspend fun writeCharacteristic(data: ByteArray): Boolean
}
```

`BleConnectionManager` implements this interface. `Bm6DataReader` depends on it. Tests use a `FakeBleConnection`.

### BleConnectionManager Changes

1. Implement `BleConnection` interface
2. Add `private val _notificationData = MutableSharedFlow<ByteArray>()` and expose as `SharedFlow`
3. Add `onCharacteristicChanged` override in GATT callback → emit bytes into `_notificationData`
4. Add `writeCharacteristic(data: ByteArray): Boolean` method wrapping GATT write
5. In `onServicesDiscovered`, after finding FFF4: call `setCharacteristicNotification(true)` and write CCCD descriptor with `ENABLE_NOTIFICATION_VALUE`

### Bm6DataReader

```
Constructor: (connection: BleConnection, protocol: Bm6Protocol)
State: StateFlow<ReadingState> — starts as Idle

requestReading():
  1. Set state → Reading
  2. Build encrypted command via protocol.buildPollCommand()
  3. Write to FFF3 via connection.writeCharacteristic(command)
  4. If write fails → set state → Error("Write failed")
  5. Collect from connection.notificationData with 5s timeout
  6. If timeout → set state → Error("Device did not respond")
  7. Parse via protocol.parseResponse(bytes)
  8. If parse fails → set state → Error("Invalid response")
  9. Set state → Success(reading)
```

### BleViewModel Changes

- Accept `Bm6DataReader` as dependency
- Expose `readingState: StateFlow<ReadingState>` from the data reader
- When `characteristicsFound` becomes true, auto-call `requestReading()`
- Add `refreshReading()` action for manual refresh

### BleScreen Changes

When connected with a successful reading, show below the connection banner:
- "Voltage: 12.45V"
- "Temperature: 24°C"
- "Last updated: [timestamp]"
- "Refresh" button

When in `Reading` state: show a loading indicator.
When in `Error` state: show error message with "Retry" button.

## Acceptance Criteria

### Functional Requirements

- [ ] `Bm6Protocol.buildPollCommand()` returns correctly encrypted bytes matching known test vector
- [ ] `Bm6Protocol.parseResponse()` correctly decrypts and parses voltage/temperature from known test vector
- [ ] `BleConnectionManager` enables FFF4 notifications during service discovery
- [ ] `BleConnectionManager` exposes notification data via `SharedFlow<ByteArray>`
- [ ] `BleConnectionManager` supports writing to FFF3 via `writeCharacteristic()`
- [ ] `Bm6DataReader.requestReading()` executes the full write → notify → parse cycle
- [ ] Reading is automatically triggered when characteristics are found after connection
- [ ] User can manually refresh the reading via a button
- [ ] Voltage displays correctly (e.g., "12.45V")
- [ ] Temperature displays correctly with sign (e.g., "24°C" or "-5°C")
- [ ] Timestamp of last reading is shown
- [ ] Error state is shown when write fails, device times out, or response is invalid
- [ ] Reading state resets to Idle on disconnect

### Non-Functional Requirements

- [ ] Notification response timeout is 5 seconds
- [ ] No new external dependencies (uses `javax.crypto` only)
- [ ] All new classes have unit tests following existing TDD patterns
- [ ] `Bm6Protocol` tests use known plaintext/ciphertext vectors from protocol documentation
- [ ] `Bm6DataReader` tests use `FakeBleConnection`, no GATT mocking

## Implementation Phases

### Step 1: Bm6Protocol — Encryption and Parsing

Create `Bm6Protocol` with:
- `buildPollCommand(): ByteArray` — encrypts the poll command
- `parseResponse(encrypted: ByteArray): Bm6Reading` — decrypts and parses response

Create `Bm6Reading` data class and `ReadingState` sealed interface.

Tests: known vector encryption, known vector decryption/parsing, negative temperature, garbage input handling.

### Step 2: BleConnection Interface Extraction

Extract `BleConnection` interface with `characteristicsFound`, `notificationData`, `writeCharacteristic()`.

Update `BleConnectionManager` to implement it.

Update existing tests to verify no regressions.

### Step 3: BleConnectionManager — Notification Enablement and Write Support

Add `SharedFlow<ByteArray>` for notification data with `onCharacteristicChanged` override.
Add `writeCharacteristic(data: ByteArray): Boolean`.
Enable FFF4 notifications + CCCD write in `onServicesDiscovered`.

Tests: notification flow emission, write delegation, notification enablement during discovery.

### Step 4: Bm6DataReader — Read Orchestration

Create `Bm6DataReader` with `requestReading()` and `StateFlow<ReadingState>`.
Create `FakeBleConnection` for testing.

Tests: successful read cycle, write failure, timeout, parse error, state transitions.

### Step 5: BleViewModel and BleScreen — Integration and UI

Extend `BleViewModel` with `Bm6DataReader`, auto-read on connect, `refreshReading()`.
Extend `BleScreen` with voltage/temperature display, refresh button, error/loading states.

Tests: ViewModel auto-read trigger, refresh action, state exposure. UI tests for all reading states.

## Edge Cases

| Edge Case | Handling |
|---|---|
| Write to FFF3 fails | `ReadingState.Error("Write failed")`, user can retry |
| No notification within 5s | `ReadingState.Error("Device did not respond")`, user can retry |
| Decryption produces garbage | `parseResponse` throws/returns error, surfaced as `ReadingState.Error` |
| Voltage is 0 | Display "0.00V" — could indicate disconnected battery, not an app error |
| Negative temperature | Handled via sign byte; displayed as e.g., "-5°C" |
| Device disconnects mid-read | `ConnectionState` changes to `Disconnected`, `ReadingState` resets to `Idle` |
| Refresh tapped while reading in progress | No-op or ignored (don't queue concurrent reads) |
| Multiple notifications received | Only first notification after write is consumed; subsequent ones ignored |

## Dependencies & Risks

| Risk | Mitigation |
|---|---|
| Protocol docs may be incomplete or inaccurate | Known test vectors validate encryption; real device testing validates parsing |
| NoPadding assumption may be wrong | If decryption fails, switch to PKCS5Padding |
| Response byte layout may differ across firmware versions | Parse defensively; surface errors clearly |
| CCCD write may fail on some OEMs | Handle descriptor write failure; show error |
| Notification may arrive before write confirmation | Collect from SharedFlow after write; ordering handled by coroutine sequencing |

## Future Phases (Out of Scope)

- **Phase 3:** Polished dashboard UI with styled cards, color-coded voltage, graphs
- **Phase 4:** Background monitoring, periodic polling, alerts, history persistence

## Sources & References

- BM6 Protocol: https://www.tarball.ca/posts/reverse-engineering-the-bm6-ble-battery-monitor/
- Android BLE Notifications: https://developer.android.com/develop/connectivity/bluetooth/ble/transfer-ble-data
- javax.crypto.Cipher: https://developer.android.com/reference/javax/crypto/Cipher
