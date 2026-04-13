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

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Android BLE API
- Min SDK 26 / Target SDK 36

## Protocol

Based on the reverse-engineered BM6 BLE protocol documented at
https://www.tarball.ca/posts/reverse-engineering-the-bm6-ble-battery-monitor/.

## License

TBD
