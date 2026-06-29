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
- ⚡ `#build` **Cache file lifecycle across ViewModel ↔ Worker boundary**: files written by the
  ViewModel (from SAF) and consumed by a background Worker must use **identical naming
  conventions**. Upload: copy SAF → temp cache → `enqueue()` → get taskId → **rename** cache
  to match the Worker's formula (`sftping_ul_<taskId>_<fileName>`). Download: Worker creates
  cache at `sftping_dl_<taskId>_<fileName>`; ViewModel computes the same path for post-transfer
  copy to SAF. Both sides use `task.id` + `task.fileName`.
- ⚡ `#build` **Never delete cache files before the Worker consumes them**. `cacheFile.delete()`
  in the ViewModel right after `enqueue()` will cause the Worker to find a missing file,
  return `Result.failure()` / `Result.retry()`, and the transfer never completes.
- `#ui` **Transfers are asynchronous** — the ViewModel must observe `TransferManager.items`
  for `COMPLETED` status on the task ID before refreshing the file list or copying download
  results. Never assume `enqueue()` completes synchronously.
- ⚡ `#build` `@HiltAndroidApp` Application class name **must not collide** with any composable function or
  other class in the same package. Renamed `SftpingApp` → `SftpingApplication` after KSP overload conflict.
- `#build` `org.json` (JSONObject, JSONArray) is an **Android framework class** and is not available in JVM
  unit tests. Add `org.json:json` as `testImplementation` for JVM test coverage.
- `#api` JSch `HostKey.getKey()` returns a **base64-encoded String**, not a `ByteArray`. Decode with
  `java.util.Base64.getDecoder().decode(key)` before computing the SHA-256 fingerprint.
- ⚡ `#api` JSch `ChannelSftp.RESUME` mode calls `stat()` on the **local** destination
  file to determine the byte offset. If the file doesn't exist (fresh download), the
  `get(..., RESUME)` call throws `SftpException`. Always ensure the local file exists
  before using RESUME mode: `if (!file.exists()) file.createNewFile()`.
- ⚡ `#api` The same applies to **uploads**: `put(..., ChannelSftp.RESUME)` `stat()`s the
  **remote** destination to find the resume offset, so a fresh upload (remote file absent)
  throws `SftpException`. You can't pre-create the remote file, so **only use RESUME when
  resuming** — route by offset: `skip == 0` → `put(..., OVERWRITE)`; `skip > 0` →
  `put(..., RESUME)`. Never use RESUME unconditionally for uploads.
- ⚡ `#api` **`ChannelSftp` is not thread-safe.** Never share one channel across concurrent
  operations — in this app the background transfer workers and the UI (`listFiles`, refresh,
  navigation, post-upload `loadFiles`) all run on `Dispatchers.IO` against the same
  `@Singleton` client, so a shared channel desyncs the SFTP request/response stream. Open a
  **fresh `ChannelSftp` per operation** over the shared `Session` (`session.openChannel("sftp")`
  → `connect()` → `try { … } finally { ch.disconnect() }`); SSH multiplexes channels safely.
  Tell-tale of a desync: JSch throws with a **garbage status id and empty message** (e.g.
  `1936916480` = `0x73730000` = "ss"); a *real* server error has a small status code (3, 4, …)
  **and** a message.
- ⚡ `#api` `callbackFlow { ... awaitClose { } }` is wrong for single-shot operations like
  SFTP downloads/uploads. `awaitClose` suspends the producer coroutine **indefinitely** after
  the operation completes, causing the `collect {}` caller to hang forever. Use `close()`
  instead — it signals normal channel completion and lets downstream logic proceed. This bug
  caused: Worker RETRY loops (WorkManager kills the hung worker), TransfersScreen stuck on
  RUNNING, `dao.updateStatus(COMPLETED)` never reached, and SAF destination never populated.
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
- `#security` Host-key fingerprints are **public keys, not secrets** → persist them
  as **plaintext JSON in DataStore** (`known_hosts`), never via Keystore (Keystore is
  only for credentials). `KnownHostsStore` is now **suspend** (DataStore is
  Flow/suspend-based); existing callers in `JschSftpClient` already run inside
  `withContext(Dispatchers.IO)`, so the sync→suspend swap is transparent.
- `#build` Tooling is intentionally bleeding-edge (AGP 9.2.1, Kotlin 2.2.10,
  compileSdk 37, Compose BOM 2025.12.00). Verify Hilt / KSP / Room versions against
  the catalog before trusting web snippets.
- `#ui` Password visibility toggle: keep it pure UI — local `remember { mutableStateOf(false) }`
  in the Composable swaps `visualTransformation` and shows an eye `trailingIcon`, only in
  password mode (hide it in key-auth mode where the path is already plaintext). No ViewModel
  or test change.
- `#ui` **SessionState bridge** for cross-ViewModel data in the no-NavHost tab app: a
  `@Singleton class SessionState @Inject constructor()` carries session-scoped values (the
  resolved start directory) from `ConnectionViewModel` → `FilesViewModel`. Hilt builds it
  with no DI module. FilesVM reads it via a default arg (`loadFiles(path =
  sessionState.initialDirectory)`), so the call site stays unchanged.
- `#api` Resolve the SFTP host home directory via `ChannelSftp.getHome()` (Kotlin property
  `home`), exposed as `ISftpClient.homeDirectory()`. Resolve once at connect
  (`enteredDir.ifBlank { homeDirectory() } ?: "/"`); do **not** auto-fallback on a later
  list failure — the Files screen shows its normal error. Mockito returns `""` for an
  unstubbed suspend `homeDirectory()`, so the resolution is null/blank-safe in tests.

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
- **Persisted KnownHostsStore via DataStore + JSON list**: model a `TrustedHost`
  (host, fingerprint, keyType, trustedAt) with `org.json` helpers (JVM-testable like
  `ConnectionProfile`); store the list under one preference key (`known_hosts`). Keep an
  `InMemoryKnownHostsStore` implementing the same suspend interface as a fast unit-test
  double. Revoke = remove the entry; the "Host key changed!" dialog's
  **Revoke & re-verify** removes the stored key, then re-runs `connect()` so the new key
  surfaces as `Unknown` for fresh TOFU verification.
- **SessionState singleton for cross-VM session data**: a trivial `@Singleton class X
  @Inject constructor()` holds resolved, session-scoped values and bridges independent
  `@HiltViewModel`s without NavHost args or `SavedStateHandle`. Used to carry the Connect
  page's default directory into the Files browser's starting path; read from a default
  function argument so existing call sites don't change.
- **Raw vs. derived list in a browser ViewModel**: keep the network-fetched listing
  (`rawFiles`) separate from the displayed list (`files`). Derive `files` through a pure
  object (`FileView.apply(raw, showHidden, query, sortMode)`) that filters (hidden
  dot-files, name search) then sorts (folders-first + chosen comparator + name tiebreaker).
  View-input setters (`setSortMode`/`toggleShowHidden`/`setSearchQuery`) update state and
  recompute **in-memory** — no re-fetch. The pure `FileView` is JVM-unit-testable; persist
  sort/hidden across navigation but clear the search query on `navigateTo`/`navigateBack`.
- **ComponentActivity + @AndroidEntryPoint** = enough for `viewModel()` + `@HiltViewModel`.
  No `hilt-navigation-compose` needed; no NavHost required for tab-based `NavigationSuiteScaffold`.
- **Material 3 swipe + multi-select list management**: `ElevatedCard` per row with
  `SwipeToDismissBox` (EndToStart only) for single-action cancel/delete. `combinedClickable`
  for long-press multi-select with `Checkbox` trailing. `selectedIds: Set<Long>` as local
  `remember` state. Contextual `TopAppBar` with `secondaryContainer` background showing
  "N selected" + delete action. Completed rows show `"Completed on yyyy-MM-dd HH:mm:ss"`.
  Tap opens a detail `AlertDialog` with file name, mono-spaced path, size, direction,
  colored status label, and formatted datetime.

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
- ADR-006: Persist trusted host keys in **DataStore as plaintext JSON** (fingerprints
  are public keys). Key entries by **host** (SSH-endpoint/port scoping deferred). Revoke
  = remove the entry; the Changed-dialog "Revoke & re-verify" re-runs `connect()`.
- ADR-007: Bridge Connect→Files with a `@Singleton SessionState` (resolved remote start
  directory) rather than NavHost args or `SavedStateHandle`; keeps the tab-based,
  no-NavHost architecture intact. Default directory is resolved once at connect
  (`enteredDir` or `getHome()`); list failures surface the normal error (no auto-fallback).
- ADR-008: Files browser computes its displayed listing from a retained `rawFiles` via a
  pure `FileView` (filter hidden + name search, then folders-first + selected sort). One
  SFTP list per directory; all view changes are in-memory and testable. Search filters the
  current directory only (no recursive remote walk).

## 📂 Code Ownership Map

| File | Touched By | Why |
|------|-----------|-----|
| `SftpingApplication.kt` | 001, 004 | @HiltAndroidApp entry point; `Configuration.Provider` for HiltWorkerFactory in 004 |
| `MainActivity.kt` | 001 | App shell, nav, @AndroidEntryPoint |
| `security/Fingerprint.kt`, `KnownHostsStore.kt`, `TrustedHost.kt` | 001, 007 | TOFU; `KnownHostsStore` persisted via DataStore (007); `TrustedHost` JSON model (007) |
| `sftp/ISftpClient.kt`, `JschSftpClient.kt` | 001, 002, 003, 007, 008 | Session in 001; transfer methods in 002; resume in 003; persist keyType (007); `homeDirectory()` (008); per-operation channel (concurrency fix) |
| `sftp/SessionState.kt` | 008 | `@Singleton` cross-VM holder for the resolved initial directory (Connect → Files) |
| `security/KeystoreCrypto.kt`, `SecretStore.kt` | 001 | Credential crypto; reusable |
| `data/connection/ConnectionProfile.kt`, `ConnectionRepository.kt` | 001, 008 | Recent connection persistence; `defaultDirectory` added in 008 |
| `data/transfer/TransferTask.kt`, `TransferDatabase.kt`, `TransferTaskDao.kt` | 003 | Room transfer-state persistence (`sftping.db`) |
| `di/SecurityModule.kt`, `SftpModule.kt` | 001, 006, 007 | Hilt bindings; `TransferStrategy` binding added in 006; KnownHostsStore→DataStore bind (007) |
| `di/DatabaseModule.kt` | 003 | Room DB + DAO providers |
| `transfer/TransferItem.kt`, `TransferManager.kt` | 002, 003, 006 | StateFlow holder in 002; Room-backed in 003; thinned to coordinator in 006 |
| `transfer/strategy/` (`TransferStrategy.kt`, `SftpTransferStrategy.kt`, `TransferProgress.kt`) | 006 | Protocol layer (JSch → `Flow<TransferProgress>`) |
| `transfer/usecase/` (Enqueue/Download/Upload/Pause/Resume/Cancel) | 003, 006 | Transfer business logic (offsets, retries, persistence) |
| `work/SftpTransferWorker.kt` | 004, 006 | Background FGS worker; delegates to use cases in 006; upload success deletes the real cache file (cleanup fix) |
| `ui/connection/` | 001, 007, 008 | Connection form + VM; trusted-hosts manager + revoke (007); password show/hide + default-directory field (008) |
| `ui/files/` (incl. `FileView.kt`) | 001, 002, 008, 009 | File browser in 001; file actions in 002; start dir seeded from `SessionState` (008); hidden toggle + sort + search via pure `FileView` (009) |
| `ui/transfers/` | 002, 004 | Transfers list, progress, pause/resume/cancel, swipe + multi-select |

## 🐛 Common Bugs Fixed
<!-- Real defects hit during development + the fix. See feature takeaways.md for context. -->

- **001** `@HiltAndroidApp` class name collided with an existing `fun SftpingApp()` composable
  in the same package → KSP overload conflict. Fix: renamed Application to `SftpingApplication`.
- **001** Host-key fingerprints came out wrong because `HostKey.getKey()` returns a
  **base64 String**, not bytes. Fix: `Base64.getDecoder().decode(key)` before SHA-256.
- **001** JVM unit tests failed to resolve `org.json.JSONObject` (Android framework class).
  Fix: add `org.json:json` as `testImplementation`.
- **002** Upload Worker found a missing cache file. Two causes: (a) the ViewModel deleted the
  cache right after `enqueue()`, and (b) ViewModel/Worker used different file names. Fix: the
  Worker owns the cache lifecycle; ViewModel renames cache to `sftping_ul_<taskId>_<fileName>`.
- **002** `rememberLauncherForActivityResult` captured an uninitialized variable (closures bind
  by reference at compose time). Fix: declare mutable state **before** the launcher.
- **003** Fresh download with `get(..., RESUME)` threw `SftpException` because RESUME `stat()`s a
  non-existent local file. Fix: `if (!file.exists()) file.createNewFile()` before RESUME.
- **003** Compose `when` over `TransferStatus` stopped compiling after adding `PAUSED`. Fix:
  add the exhaustive branches in `statusText()`/icon mapping.
- **004** ⚡ App crashed at runtime with `InvalidForegroundServiceTypeException: Starting FGS
  with type none`. Fix: pass `FOREGROUND_SERVICE_TYPE_DATA_SYNC` to `ForegroundInfo`.
- **004** ⚡ Second FGS crash: `0x00000001 is not a subset of 0x00000000` (WorkManager's
  `SystemForegroundService` declares no type). Fix: manifest merge directive adding
  `android:foregroundServiceType="dataSync"` to that service.
- **004** `CoroutineWorker`'s `context` constructor param shadowed `android.content.Context`.
  Fix: use `applicationContext` (or rename the param).
- **006** ⚡ Transfers hung on RUNNING and Workers entered RETRY loops because `callbackFlow`
  used `awaitClose {}` for a single-shot SFTP op (suspends the producer forever). Fix: call
  `close()` after the operation returns. `awaitClose` is only for long-lived listeners.
- **006** `mock<Context>()` returned null `cacheDir`, breaking use-case tests. Fix:
  `doReturn(realDir).whenever(context).cacheDir`.
- **fix (upload)** ⚡ Fresh uploads failed instantly (`Worker result FAILURE`, ~30 ms)
  because `SftpTransferStrategy.upload` always called `uploadWithResume` → `put(..., RESUME)`,
  which `stat()`s the absent remote file and throws `SftpException` (the upload twin of the
  003 download-RESUME bug); the real cause was swallowed (no log). Fix: route by offset in
  the strategy — `skip == 0` → `sftpClient.upload` (OVERWRITE), `skip > 0` → `uploadWithResume`
  (RESUME); plus `Log.e` the exception in `Upload/DownloadUseCase`.
- **fix (concurrency)** ⚡ The 2nd of two uploads failed with a JSch garbage-status
  `SftpException` (`1936916480` = `0x73730000`, empty message) because `JschSftpClient` shared
  **one `ChannelSftp`** across concurrent transfers + UI `listFiles` (non-thread-safe → SFTP
  stream desync). Fix: open a **fresh sftp channel per operation** over the shared session and
  disconnect it in `finally`; `connect()`/`trustAndProceed()` no longer keep a persistent
  channel. Also fixed an upload cache leak: the worker's success cleanup deleted the stale
  pre-rename `task.localUri`; now it deletes the real cache file
  `sftping_ul_<id>_<name>`.

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
