# sftping — Agent Guide

## Project Summary

`sftping` is an Android **SFTP client** built with **Kotlin + Jetpack Compose**.
Single `:app` module, package `com.example.sftping`, app shell in
`MainActivity.kt`. Build system is Gradle Kotlin DSL with a version catalog at
`gradle/libs.versions.toml`. Key facts: minSdk 28, targetSdk 36, compileSdk 37,
`applicationId = com.example.sftping`.

It can connect with SHA-256 host-key verification (TOFU), browse a remote
filesystem, and run resumable, pausable, background uploads/downloads. See
`README.md` for the user-facing overview.

## Architecture / Module Layout

Single-Activity app. `MainActivity` hosts a Material 3 `NavigationSuiteScaffold`
with four state-driven tabs: **Connect → Files → Transfers → Editor** (no NavHost).

Stack: **Hilt** DI, **Room** (`sftping.db` for transfers, `sftping_editor.db` for
offline editor edits), **WorkManager** `dataSync` foreground service for background
transfers, **DataStore** for connection profiles, trusted host keys, saved editor
locations + encrypted secrets, **JSch (mwiede fork)** for SFTP, and **Android
Keystore** AES-256-GCM for credential encryption.

Transfers use a layered, protocol-agnostic pipeline:
`SftpTransferWorker → Download/UploadUseCase → TransferStrategy (SftpTransferStrategy
→ JschSftpClient)`, with `TransferManager` exposing a `StateFlow` over the Room DAO.

Package map under `com/example/sftping/`:

- `ui/{connection,files,transfers,editor,theme}` — Compose screens + `@HiltViewModel`s
- `transfer/` — `TransferManager` (thin state holder) + `strategy/` + `usecase/`
- `sftp/` — `ISftpClient`/`JschSftpClient` (incl. `readText`/`writeText`), `RemoteFile`, `HostKeyResult`, `SessionState` (incl. `connected` StateFlow + `pendingEditPath` Files→Editor bridge)
- `security/` — `Fingerprint`, `KnownHostsStore` (DataStore-backed), `TrustedHost`, `KeystoreCrypto`, `SecretStore`
- `util/` — `Clipboard` (interface + `AndroidClipboard` + `InMemoryClipboard` double)
- `data/connection/` (DataStore) + `data/transfer/` (Room) + `data/editor/` (DataStore locations + Room `sftping_editor.db` pending edits)
- `work/` — `SftpTransferWorker` (`@HiltWorker`)
- `di/` — `SftpModule`, `SecurityModule`, `DatabaseModule`, `EditorModule`, `ClipboardModule`

> In the Files tab, long-press an item to open a context menu: **Copy path**
> (to clipboard), **Edit** (text-type files only — opens transiently in the Editor
> tab), and **Select** (enters multi-select).

> Known gaps: private-key auth UI exists but isn't wired in `JschSftpClient`.
> The remote editor is text-only, one file at a time, edit-existing-only, and
> saves last-write-wins (no server-side conflict detection). The **Edit** action is
> gated on file *type*, not permissions — SFTP can't predict write access, so a
> denied write surfaces as a save-time error. See `README.md` and `MEMORY.md`.

## Commands

- Build: `./gradlew assembleDebug`
- Unit tests (JVM): `./gradlew testDebug`
- Instrumented/UI tests (device/emulator): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint`

## Conventions

- Add/bump dependencies in `gradle/libs.versions.toml`, not inline in
  `build.gradle.kts`.
- Pure logic lives in testable classes (covered by `app/src/test/`); keep
  Composables thin. Instrumented/UI tests live in `app/src/androidTest/`.
- Never commit secrets — `local.properties`, keystores, and `.env`-style files
  are gitignored; keep it that way.

## Triggering Feature Development

When the user describes a new feature (creates, builds, adds, wants a new screen,
etc.), follow the spec-driven development workflow in `specs/SDD.md`. Read
`MEMORY.md` before writing any spec to avoid repeating known bugs. The workflow:
1. Generate mockups if needed (`canvas-design` + `theme-factory`)
2. Co-author spec + plan (`doc-coauthoring`)
3. Write test plan and tasks
4. Implement one commit per task
5. Write takeaways → promote to `MEMORY.md`
