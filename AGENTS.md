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
with three state-driven tabs: **Connect → Files → Transfers** (no NavHost).

Stack: **Hilt** DI, **Room** (`sftping.db`) for transfer state, **WorkManager**
`dataSync` foreground service for background transfers, **DataStore** for
connection profiles + encrypted secrets, **JSch (mwiede fork)** for SFTP, and
**Android Keystore** AES-256-GCM for credential encryption.

Transfers use a layered, protocol-agnostic pipeline:
`SftpTransferWorker → Download/UploadUseCase → TransferStrategy (SftpTransferStrategy
→ JschSftpClient)`, with `TransferManager` exposing a `StateFlow` over the Room DAO.

Package map under `com/example/sftping/`:

- `ui/{connection,files,transfers,theme}` — Compose screens + `@HiltViewModel`s
- `transfer/` — `TransferManager` (thin state holder) + `strategy/` + `usecase/`
- `sftp/` — `ISftpClient`/`JschSftpClient`, `RemoteFile`, `HostKeyResult`
- `security/` — `Fingerprint`, `KnownHostsStore`, `KeystoreCrypto`, `SecretStore`
- `data/connection/` (DataStore) + `data/transfer/` (Room entity/DAO/db)
- `work/` — `SftpTransferWorker` (`@HiltWorker`)
- `di/` — `SftpModule`, `SecurityModule`, `DatabaseModule`

> Known gaps: `KnownHostsStore` is in-memory (trust lost on restart); private-key
> auth UI exists but isn't wired in `JschSftpClient`. See `README.md` and `MEMORY.md`.

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
