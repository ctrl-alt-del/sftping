# MEMORY — Accumulated Project Knowledge

## 🧠 Tech Gotchas
<!-- Tagged: #api #ui #build #security — AI searches by tag -->
<!-- ⚡ = broke in production / non-negotiable guardrail -->

- ⚡ `#build` Hilt Gradle plugin must be **≥ 2.59.2** for AGP 9.x compatibility. Versions ≤ 2.56.2 crash with
  "Android BaseExtension not found" because AGP 9 removed the old `BaseExtension` API.
- ⚡ `#build` KSP versioning is **independent** of Kotlin. For Kotlin 2.2.10, use KSP **2.3.9**, not
  `2.2.10-1.0.x` (the `<kotlin>-<ksp>` pattern is dead for KSP2).
- ⚡ `#build` `androidx.core:core-ktx:1.19.0` requires **compileSdk ≥ 37**. If compileSdk is 36, bump it or
  downgrade core-ktx.
- ⚡ `#build` On **API 34+** (targetSdk ≥ 34), every foreground service must declare its type.
  WorkManager's `ForegroundInfo` constructor **must** pass `FOREGROUND_SERVICE_TYPE_DATA_SYNC` as
  the third argument, otherwise `InvalidForegroundServiceTypeException: Starting FGS with type none`
  crashes the app at runtime.
- ⚡ `#build` WorkManager's bundled `SystemForegroundService` has **no foregroundServiceType** in its
  library manifest (value `0x00000000`). Even after fixing `ForegroundInfo`, a second crash occurs:
  `0x00000001 is not a subset of 0x00000000`. Fix by adding a **manifest merge directive**:
  ```xml
  <service android:name="androidx.work.impl.foreground.SystemForegroundService"
      android:foregroundServiceType="dataSync" tools:node="merge" />
  ```
  Both fixes are required — `ForegroundInfo` carries the type at runtime, and the manifest
  declares it at install time.
- ⚡ `#build` `@HiltAndroidApp` Application class name **must not collide** with any composable function or
  other class in the same package. Renamed `SftpingApp` → `SftpingApplication` after KSP overload conflict.
- `#build` `org.json` (JSONObject, JSONArray) is an **Android framework class** and is not available in JVM
  unit tests. Add `org.json:json` as `testImplementation` for JVM test coverage.
- `#api` JSch `HostKey.getKey()` returns a **base64-encoded String**, not a `ByteArray`. Decode with
  `java.util.Base64.getDecoder().decode(key)` before computing the SHA-256 fingerprint.
- `#ui` `viewModel()` from `lifecycle-viewmodel-compose` works with `@HiltViewModel` + `@AndroidEntryPoint`
  on the Activity — no need for `hilt-navigation-compose` or `hiltViewModel()`.
- `#ui` ViewModel properties using `by mutableStateOf(...)` need explicit imports:
  `androidx.compose.runtime.getValue`, `setValue`, `mutableStateOf`.
- `#ui` `rememberLauncherForActivityResult` closures capture by reference at compose time.
  **Declare mutable state variables before the launcher** that references them, or the compose
  compiler will report the variable as uninitialized.
- `#ui` `withContext(Dispatchers.IO)` is a `suspend` function — any helper that uses it must
  be `suspend`. A non-suspend `fun` returning `= withContext(...)` will not compile.
- `#ui` SAF `ContentResolver.query` for `_display_name` can return null on some content
  providers. Always fall back to `uri.lastPathSegment` for the file name.
- `#ui` TransferManager + TransfersViewModel pattern: a `@Singleton` state holder publishes
  `StateFlow<List<TransferItem>>`, wrapped by a thin `@HiltViewModel` for Compose access.
  Scales naturally to Room-backed persistence in 003.
- `#ui` Multi-select in LazyColumn via `combinedClickable`: tap = enter/action, long-press =
  toggle selection. Selection state is a `List<String>` of paths in the ViewModel. The app
  bar switches to a contextual bar when selection is active.
- `#api` Room's generated suspend DAO functions (e.g. `dao.updateProgress()`) cannot be
  called from non-suspend callbacks like progress lambdas `(Long, Long) -> Unit`. Wrap
  them in `scope.launch { dao.updateProgress(...) }` to bridge the gap.
- `#api` JSch `ChannelSftp.RESUME` handles remote offset automatically: `get(src, dst,
  monitor, RESUME)` skips `localFile.length()` bytes on the remote; `put(stream, dst,
  monitor, RESUME)` skips `remoteFile.length()` bytes. However, for upload the
  `InputStream` should be manually `skip()`ped to avoid re-reading already-uploaded bytes.
- `#build` `runTest` with `CoroutineScope(Dispatchers.IO)` in the subject under test
  uses real time for delays — not virtual time. Avoid testing async completion races on
  real dispatchers; prefer fake scopes or check only initial state.
- `#build` Room KSP overrides constructor default parameter values in generated code.
  When constructing Room entities in tests, use **named parameters** or pass all
  arguments explicitly — positional args with defaults don't work reliably.
- `#ui` `StateFlow.first()` returns the immediately cached value, not the next emission.
  To wait for a state change, use `first { predicate }` or `filter { }.first()`.
- ⚡ `#api` Use the **mwiede JSch fork** (`com.github.mwiede:jsch`, ≥ 0.2.15), never
  `com.jcraft:jsch` (abandoned at 0.1.55, vulnerable to Terrapin / CVE-2023-48795).
- `#api` JSch resume signatures: download is
  `ChannelSftp.get(src, OutputStream, monitor, ChannelSftp.RESUME, skip)`; upload is
  `put(InputStream, dst, monitor, ChannelSftp.RESUME)`. The PRD's
  `get(remote, out, localSize, monitor)` argument order is **wrong** — go through
  the `ISftpClient` abstraction and map carefully.
- ⚡ `#ui` SAF `ContentResolver.openOutputStream` cannot be arbitrarily seeked, and
  append mode (`"wa"`) support is provider-dependent → resumable **downloads** must
  write to an app-private (seekable) cache file, then copy to the SAF destination on
  completion.
- `#build` WorkManager workers have a ~10-minute execution window, and `dataSync`
  foreground services have a ~6h/day cap on Android 15+ → implement pause as
  **stop worker + persist byte offset**, resume as **re-enqueue from offset**.
  Never a `while (isPaused) delay()` busy-wait.
- ⚡ `#security` `androidx.security:security-crypto` (EncryptedSharedPreferences) is
  deprecated (last release 1.1.0-alpha06) → encrypt credentials with **Android
  Keystore AES-GCM** (optionally Tink) and store ciphertext in DataStore.
- `#security` Show host-key fingerprints as **SHA-256** (TOFU), not MD5; never use
  `StrictHostKeyChecking=no` in production.
- `#build` Tooling is intentionally bleeding-edge (AGP 9.2.1, Kotlin 2.2.10,
  compileSdk 37, Compose BOM 2025.12.00). Verify Hilt / KSP / Room versions against
  the catalog before trusting web snippets.

## 🔧 Patterns That Worked
<!-- Reusable patterns discovered across features -->

- **Post-connect host-key verification**: connect at JSch level first, extract session host key,
  compute SHA-256 fingerprint, check KnownHostsStore in the app layer. Return a sealed class
  (`HostKeyResult.Trusted | Unknown | Changed`) to the ViewModel UiState. Decouples JSch from
  UI threading and avoids JSch's built-in `HostKeyRepository` API.
- **DataStore + JSON for simple object lists**: serialize/deserialize `List<ConnectionProfile>`
  as a single `stringPreferencesKey` JSON blob. Avoids Proto DataStore overhead for small,
  non-binary data.
- **Credentials outside the profile**: passwords/keys are encrypted and stored separately via
  `SecretStore` keyed by `host:port:user` ID, not bundled in `ConnectionProfile`. Keeps
  DataStore records audit-clean (profiles = public, keys = encrypted, stored separately).
- **ComponentActivity + @AndroidEntryPoint** = enough for `viewModel()` + `@HiltViewModel`.
  No `hilt-navigation-compose` needed; no NavHost required for tab-based `NavigationSuiteScaffold`.

## 📐 Architecture Decisions
<!-- ADRs made during spec-driven development -->
- ADR-001: Specs in specs/ separate from docs/ (permanent reference vs per-feature artifacts)
- ADR-002: SFTP library = JSch (mwiede fork), not SSHJ — lighter, BSD-3-Clause,
  matches PRD, Terrapin-patched.
- ADR-003: Transfer pause = stop worker + persist byte offset; resume = re-enqueue
  from offset (avoids WorkManager / FGS time limits).
- ADR-004: Resumable downloads land in a private cache file, then copy to the SAF
  target (SAF is not reliably seekable).
- ADR-005: DI = Hilt (with `HiltWorker` for WorkManager). ⚡ `applicationId` stays
  `com.example.sftping` until release — **must** change before Play Store submission
  (rejects `com.example.*`).

## 📂 Code Ownership Map

| File | Touched By | Why |
|------|-----------|-----|
| `SftpingApplication.kt` | 001 | @HiltAndroidApp entry point |
| `MainActivity.kt` | 001 | App shell, nav, @AndroidEntryPoint |
| `sftp/ISftpClient.kt`, `JschSftpClient.kt` | 001, 002, 003 | Session in 001; transfer methods in 002; resume in 003 |
| `sftp/RemoteFile.kt`, `HostKeyResult.kt` | 001 | Domain models |
| `security/Fingerprint.kt`, `KnownHostsStore.kt` | 001 | TOFU; may become DataStore-backed |
| `security/KeystoreCrypto.kt`, `SecretStore.kt` | 001 | Credential crypto; reusable |
| `data/connection/ConnectionProfile.kt`, `ConnectionRepository.kt` | 001 | Recent connection persistence |
| `di/SecurityModule.kt`, `SftpModule.kt` | 001 | Hilt bindings |
| `ui/connection/` | 001 | Connection form + VM |
| `ui/files/` | 001, 002 | File browser in 001; upload/delete/rename actions in 002 |

## 🐛 Common Bugs Fixed

## 🧠 AI Workflow Rule

Before writing any spec, read in order:
1. `AGENTS.md` or `CLAUDE.md` — project conventions
2. `specs/SDD.md` — SDD workflow
3. `MEMORY.md` — search for relevant #tags
4. `specs/index.md` — check for feature file conflicts

After shipping a feature:
1. Write `takeaways.md` in the feature folder
2. Curate findings into `MEMORY.md` (tagged, ⚡ for critical)
3. Update code ownership map
