# sftping

[![CI](https://github.com/ctrl-alt-del/sftping/actions/workflows/ci.yml/badge.svg)](https://github.com/ctrl-alt-del/sftping/actions/workflows/ci.yml)

An Android **SFTP client** built with **Kotlin + Jetpack Compose**. Connect to a
remote host, browse its filesystem, and move files in either direction with
resumable, pausable, background transfers.

- **Single `:app` module**, package `com.example.sftping`
- **minSdk 28** · **targetSdk 36** · **compileSdk 37**
- **License:** Apache-2.0

## Features

- **Connect with host-key verification (TOFU).** On first connect the app shows
  the server's **SHA-256** fingerprint and asks you to trust it. Subsequent
  connections are checked against the stored fingerprint and surface a
  `Trusted` / `Unknown` / `Changed` result (MITM warning on change). Trusted keys
  are **persisted** (DataStore) so they survive restarts, and can be **revoked**
  from a trusted-hosts manager or re-verified directly from the change warning.
- **Remote file browser** with folder navigation, **multi-select**, **search**,
  **sort** (name A–Z/Z–A, size, last modified), and a **hidden-file toggle**.
  Long-press an item for a context menu: **copy its remote path**, **edit** it (for
  text-type files, opens in the Editor tab), or **select** for multi-select. Returns
  to your last folder when you switch tabs.
- **Upload & download via the Storage Access Framework** — pick multiple documents
  to upload at once (with an in-app confirmation sheet that flags
  already-uploaded files), or select a destination folder for multi-download.
- **Convenient connect form.** Show/hide the password, and set an optional default
  directory (defaults to the host's home) that the file browser opens on connect.
- **Resumable transfers.** Byte offsets are persisted in Room; interrupted
  transfers resume from where they stopped (JSch `RESUME` mode).
- **Background transfers.** Each transfer runs in a WorkManager `dataSync`
  foreground service with a live progress notification, surviving app
  backgrounding and screen-off.
- **Pause / resume / cancel** from the Transfers screen, and **retry a failed upload** individually or **retry all failed uploads** at once. Transfers are grouped into Active, Failed (collapsible), and Completed (collapsible) sections.
- **Remote text editor.** A dedicated **Editor** tab manages a list of saved
  remote file locations (add / edit / delete, persisted across restarts and
  usable even while disconnected). Open a location to edit a text file in place
  with **autosave**, **undo/redo**, and a manual **save**. Edits made while
  offline are **cached locally** (Room, `sftping_editor.db`) and **re-synced** to
  the server on reconnect; the editor greys out when not connected.
- **Encrypted credentials at rest.** Passwords are sealed with an
  **Android Keystore** AES-256-GCM key and stored as ciphertext in DataStore,
  separate from the (non-secret) connection profile.

## Tech Stack

| Area | Library | Version |
|------|---------|---------|
| Language / build | Kotlin / AGP | 2.2.10 / 9.2.1 |
| UI | Jetpack Compose (BOM) | 2025.12.00 |
| UI | Material 3 + Adaptive Navigation Suite | via BOM |
| DI | Dagger Hilt (+ KSP) | 2.59.2 / KSP 2.3.9 |
| SFTP | JSch (mwiede fork) | 0.2.25 |
| Persistence | Room | 2.7.1 |
| Preferences | DataStore Preferences | 1.1.4 |
| Background work | WorkManager (+ Hilt Work) | 2.10.1 / 1.2.0 |
| Crypto | Android Keystore (AES-256-GCM) | platform |
| Async | kotlinx-coroutines | 1.10.1 |

Tooling is intentionally bleeding-edge. Dependencies are managed in the version
catalog at [`gradle/libs.versions.toml`](gradle/libs.versions.toml) — add or bump
versions there, not inline in `build.gradle.kts`.

## Architecture

Single-Activity app. `MainActivity` hosts a Material 3 `NavigationSuiteScaffold`
with four tabs whose visibility is state-driven (no NavHost):

```
Connect  ──connected──▶  Files  ──"Not connected"──▶  Connect
                          │
                          ├─ enqueue ─▶  Transfers (always accessible)
                          │
                          └─────────▶  Editor (always accessible; edits gated on connection)
```

Transfers flow through a layered, protocol-agnostic pipeline so new transports
(FTP/SCP) can be added without touching the orchestrator:

```
SftpTransferWorker  ──▶  DownloadUseCase / UploadUseCase  ──▶  TransferStrategy
   (Android lifecycle:        (business logic:                  (protocol:
    FGS + notification)        offsets, retries,                 SftpTransferStrategy
                               DAO persistence)                  → JschSftpClient)
                                       │
                                       ▼
              Room (sftping.db)  ──Flow──▶  TransferManager (StateFlow)  ──▶  UI
```

Dependency injection is wired with Hilt through four modules:

- **`SftpModule`** — binds `JschSftpClient → ISftpClient`, `SftpTransferStrategy → TransferStrategy`
- **`SecurityModule`** — binds `KnownHostsStore`, provides the `AndroidKeyStore`
- **`DatabaseModule`** — provides the Room `TransferDatabase` (`sftping.db`) and DAO
- **`EditorModule`** — provides the Room `EditorDatabase` (`sftping_editor.db`) + DAO and binds `EditorLocationRepository`

### Project structure

```
app/src/main/java/com/example/sftping/
├── MainActivity.kt            App shell, 4-tab navigation
├── SftpingApplication.kt      @HiltAndroidApp + WorkManager Configuration.Provider
├── ui/
│   ├── connection/            Connect form + host-key dialog (ViewModel)
│   ├── files/                 Remote browser, long-press actions (copy path / edit / select), SAF upload/download
│   ├── transfers/             Active/Failed/Completed grouped list, collapsible sections, retry-all, swipe-to-cancel, detail dialog
│   ├── editor/                Remote text editor: locations list, editor pane, UndoStack (ViewModel)
│   └── theme/                 Material 3 theme (dynamic color on Android 12+)
├── transfer/
│   ├── TransferManager.kt     Thin @Singleton StateFlow holder over the DAO
│   ├── strategy/              TransferStrategy + SftpTransferStrategy + TransferProgress
│   └── usecase/               Enqueue/Download/Upload/Pause/Resume/Cancel
├── sftp/                      ISftpClient, JschSftpClient, RemoteFile, HostKeyResult, SessionState
├── security/                  Fingerprint, KnownHostsStore, TrustedHost, KeystoreCrypto, SecretStore
├── util/                      Clipboard (interface + AndroidClipboard + InMemoryClipboard)
├── data/
│   ├── connection/            ConnectionProfile + DataStore-backed repository
│   ├── transfer/              Room entity, DAO, database (sftping.db)
│   └── editor/                EditorLocation (DataStore) + PendingEdit Room DB (sftping_editor.db)
├── work/                      SftpTransferWorker (@HiltWorker, foreground service)
└── di/                        SftpModule, SecurityModule, DatabaseModule, EditorModule, ClipboardModule
```

## Build & Run

Prerequisites: Android SDK with **platform 37** installed, and **JDK 11+**.

```bash
# Build a debug APK
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug
```

## CI (GitHub Actions)

Every push to `master` and every PR triggers a CI run (`.github/workflows/ci.yml`):
`lint` → `assembleDebug` → `testDebug` on `ubuntu-latest` with JDK 21.

Android SDK platform 37 is cached (~150 MB) via `actions/cache@v4` so subsequent
runs skip the download. The platform is hosted in the `ci-assets` branch while
Google's public SDK repository does not yet include API 37. When it does, the
download can be replaced with a single `sdkmanager` call.
project has no UI tests yet). See `AGENTS.md` for details on adding them.

## Testing

```bash
./gradlew testDebug                  # JVM unit tests (fast)
./gradlew connectedDebugAndroidTest  # instrumented tests (device/emulator)
./gradlew lint                       # Android lint
```

Pure logic lives in testable classes covered by JVM unit tests in
`app/src/test/` (11 suites: SFTP path building, fingerprints, transfer use cases,
strategy progress, view models, etc.). The single instrumented test in
`app/src/androidTest/` exercises the real Android Keystore crypto round-trip.

## Known Limitations & Roadmap

- **Private-key auth is not wired.** The connection UI exposes a key toggle, but
  `JschSftpClient` currently authenticates with password / keyboard-interactive
  only.
- **Release build is unminified** (R8 disabled) and the **`applicationId` is
  `com.example.sftping`** — both must change before a Play Store release
  (Google rejects `com.example.*`).
- **The remote editor is text-only, one file at a time, and edit-existing-only**
  (no create-on-save). Saves are **last-write-wins** with no server-side conflict
  detection.

## Development Workflow

This project follows a spec-driven development (SDD) process. Before building a
feature, read [`AGENTS.md`](AGENTS.md) (conventions), [`specs/SDD.md`](specs/SDD.md)
(workflow), and [`MEMORY.md`](MEMORY.md) (accumulated gotchas). Per-feature specs,
plans, test plans, and takeaways live under [`specs/`](specs/), indexed in
[`specs/index.md`](specs/index.md).

## License

Licensed under the [Apache License 2.0](LICENSE). Third-party notices: JSch
(mwiede fork) is BSD-3-Clause; AndroidX/Compose libraries are Apache-2.0.
