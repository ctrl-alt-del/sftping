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

- `ui/{connection,files,transfers,editor,theme,components}` — Compose screens + `@HiltViewModel`s + shared components
- `transfer/` — `TransferManager` (thin state holder) + `strategy/` + `usecase/`
- `sftp/` — `ISftpClient`/`JschSftpClient` (incl. `readText`/`writeText`, `@Volatile session`, dead-session detection in `openChannel()`), `RemoteFile`, `HostKeyResult`, `SessionState` (incl. `connected` StateFlow + `pendingEditPath` StateFlow Files→Editor bridge)
- `security/` — `Fingerprint`, `KnownHostsStore` (DataStore-backed), `TrustedHost`, `KeystoreCrypto`, `SecretStore`
- `util/` — `Clipboard` (interface + `AndroidClipboard` + `InMemoryClipboard` double)
- `data/connection/` (DataStore) + `data/transfer/` (Room) + `data/editor/` (DataStore locations + Room `sftping_editor.db` pending edits)
- `work/` — `SftpTransferWorker` (`@HiltWorker`)
- `di/` — `SftpModule`, `SecurityModule`, `DatabaseModule`, `EditorModule`, `ClipboardModule`

> In the Files tab, long-press an item to open a context menu: **Copy path**
> (to clipboard), **Edit** (text-type files only — opens transiently in the Editor
> tab), and **Select** (enters multi-select).
>
> A thin green/red `ConnectionIndicator` bar sits above the TopAppBar on Files,
> Transfers, and Editor tabs. Green when connected, red when disconnected.
> Reacts to `SessionState.connected` via ViewModel `collect` — no polling. (018)

> Known gaps: private-key auth UI exists but isn't wired in `JschSftpClient`.
> The remote editor is text-only, one file at a time, edit-existing-only, and
> saves last-write-wins (no server-side conflict detection). The **Edit** action is
> gated on file *type*, not permissions — SFTP can't predict write access, so a
> denied write surfaces as a save-time error. The Files→Editor handoff is a
> `pendingEditPath` **StateFlow** bridged into the EditorViewModel's init collect
> (015, hardened reactively in 019); a file opened while disconnected stays
> read-only until a reconnect reloads it (019). See `README.md` and `MEMORY.md`.

## Commands

- Build: `./gradlew assembleDebug`
- Unit tests (JVM): `./gradlew testDebug`
- Instrumented/UI tests (device/emulator): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint`

## CI (GitHub Actions)

CI runs on every push to `master` and every PR targeting `master`.
Workflow: `.github/workflows/ci.yml`.

- **Runner:** `ubuntu-latest` with JDK 21 (Temurin), Gradle 9.4.1 (wrapper).
- **SDK:** Android platform 37, downloaded on first run from the `ci-assets`
  branch (~60 MB compressed, ~164 MB cached uncompressed). GitHub runners do
  not yet pre-install platforms ≥ 35, and Google has not published API 37 in
  the public SDK repository yet. Once available via `sdkmanager`, the download
  step can be replaced with `sdkmanager "platforms;android-37"`.
- **Steps:** `lint` → `assembleDebug` → `testDebug`. Lint runs first for faster
  failure feedback.
- **No instrumented tests** — not included because `app/src/androidTest/` has
  no tests. To add them, create a separate job with an emulator (using
  `reactivecircus/android-emulator-runner@v2`).
- **Caching:** Gradle caches via `gradle/actions/setup-gradle@v4`. Same-repo PRs
  can write to the cache; fork PRs are read-only (cache poisoning prevention).
  The SDK platform cache uses `actions/cache@v4` keyed `android-sdk-37-linux-v4`.
- **When GitHub pre-installs API 37:** the SDK download step (and `ci-assets`
  branch) can be removed without breaking the workflow.

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
`MEMORY.md` before writing any spec to avoid repeating known bugs. Check
`constitution.md` for governing principles. The workflow:
1. Generate mockups if needed (`canvas-design` + `theme-factory`)
2. Co-author spec + plan (`doc-coauthoring`); resolve all `[NEEDS CLARIFICATION]` markers
3. Run requirements quality checklist
4. Write test plan and tasks
5. Run cross-artifact analysis (spec ↔ plan ↔ tasks)
6. Implement one commit per task
7. Run convergence check (assess codebase against spec/plan/tasks)
8. Write takeaways → promote to `MEMORY.md`
