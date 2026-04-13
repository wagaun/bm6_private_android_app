# BM6 Monitor - Agent Guidelines

## Project Overview

Privacy-focused Android app for the BM6 BLE battery monitor. No analytics, no network calls, no telemetry.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **BLE:** Raw Android BLE API (no Nordic library)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36
- **Build:** Gradle 8.11.1, AGP 8.9.0, JDK 17
- **Testing:** JUnit 4 + Robolectric

## Conventions

### Code Style

- Kotlin with trailing commas
- Package: `com.bm6.monitor`
- BLE classes live under `com.bm6.monitor.ble`
- UI/theme classes live under `com.bm6.monitor.ui.theme`

### Testing

- TDD: write tests first, then implementation
- Unit tests use Robolectric for API-level-dependent logic
- Test files mirror source structure under `app/src/test/`
- Use `@Config(sdk = [...])` to test across API levels

### Git Workflow

- Branch per step: `feat/step-N-description`
- Commit after each logical step
- Commit messages: imperative mood, explain the "why"
- Co-author tag: `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>`

### BLE Permission Model

Two permission sets depending on API level:
- API 31+: `BLUETOOTH_SCAN` (with `neverForLocation`), `BLUETOOTH_CONNECT`
- API 26-30: `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION`

Always branch on `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`.

### Architecture

- ViewModel + StateFlow for state management
- Single-activity Compose app
- BLE logic in dedicated classes, not in Activity/ViewModel directly

## Project Tracker

project_tracker: github

## BM6 Protocol Reference

- BLE characteristics: FFF3 (write/command), FFF4 (notify/data)
- AES-CBC encryption, key: `[108,101,97,103,101,110,100,255,254,48,49,48,48,48,48,57]`, IV: 16 null bytes
- Poll command (plaintext): `d1550700000000000000000000000000`
- Voltage: `int(decrypted[15:18], 16) / 100` volts
- Temperature: byte 6 = sign (`01` = negative), byte 8 = magnitude in Celsius
- Full protocol: https://www.tarball.ca/posts/reverse-engineering-the-bm6-ble-battery-monitor/
