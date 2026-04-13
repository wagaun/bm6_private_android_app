# BM6 Monitor

A privacy-focused Android app for monitoring vehicle battery voltage and temperature via the BM6 BLE battery monitor (also sold as Sealey BT2020).

The official BM6 app collects invasive telemetry including GPS and device data. This open-source alternative communicates directly with the device over Bluetooth Low Energy — no network calls, no tracking, no analytics.

## Status

**Work in progress** — Phase 1: project scaffold and BLE connection.

## Requirements

- Android 8.0+ (API 26)
- Device with Bluetooth Low Energy support
- BM6 battery monitor hardware

## Building

1. Install [Android Studio](https://developer.android.com/studio) or the Android SDK command-line tools
2. Clone the repo
3. Create `local.properties` with your SDK path:
   ```
   sdk.dir=/path/to/Android/sdk
   ```
4. Build:
   ```
   ./gradlew assembleDebug
   ```

## Running Tests

```
./gradlew testDebugUnitTest
```

Tests use [Robolectric](https://robolectric.org/) to simulate different Android API levels without a device.

## Tech Stack

- Kotlin 2.1.10
- Jetpack Compose (Material 3, BOM 2025.04.00)
- Android BLE API
- Min SDK 26 / Target SDK 36
- AGP 8.9.0 / Gradle 8.11.1
- JDK 17

## Project Structure

```
app/src/main/java/com/bm6/monitor/
├── MainActivity.kt              # Entry point with Compose UI
├── ble/
│   └── BlePermissionHelper.kt   # BLE permission handling (API 26-30 + 31+)
└── ui/theme/
    ├── Color.kt                 # Green color palette
    ├── Theme.kt                 # Material 3 theme with dynamic color
    └── Type.kt                  # Typography

app/src/test/java/com/bm6/monitor/
└── ble/
    └── BlePermissionHelperTest.kt  # Robolectric tests for permission logic
```

## BLE Permissions

The app handles two different Android BLE permission models:

| API Level | Permissions Required |
|---|---|
| 26–30 | `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION` |
| 31+ | `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` |

Location is required on older Android versions because BLE scan results can infer physical location. Android 12+ introduced dedicated BLE permissions with a `neverForLocation` flag.

## Protocol

Based on the reverse-engineered BM6 BLE protocol documented at
https://www.tarball.ca/posts/reverse-engineering-the-bm6-ble-battery-monitor/.

## License

TBD
