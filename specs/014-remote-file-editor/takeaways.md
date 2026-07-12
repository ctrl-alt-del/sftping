# Remote File Editor — Takeaways

## What Went Well
- Front-loading the design decisions with two rounds of `question` (durability,
  autosave routing, conflict policy, re-sync trigger, undo mechanism) meant the
  spec had zero open questions and the implementation never had to guess.
- Slicing into 6 build-verifiable commits (plumbing → locations → cache DB →
  UndoStack → ViewModel → UI) let each layer be tested in isolation. The pure
  `UndoStack` and JSON model landed with fast JVM coverage before any Compose.
- Reusing established patterns kept the diff small: `EditorLocation` mirrors
  `ConnectionProfile`/`TrustedHost` (org.json + DataStore), the
  interface + `InMemory*` double mirrors `KnownHostsStore`, and the `FakeDao`
  test pattern from `TransferManagerTest` transferred directly to `PendingEdit`.

## What We Learned
- **Hilt `@Inject constructor` can't have a defaulted primitive param** (e.g.
  `autosaveDelayMs: Long = 2_000L`) — there's no binding for `Long`. Made it an
  `internal var` overridable in tests instead.
- **`EditorLocationRepository` had to become an interface.** A concrete
  DataStore-backed class isn't unit-testable (needs `Context`), so — like
  `KnownHostsStore` — it's now `interface + DataStore impl + InMemory* double`,
  bound via `@Binds`. Retrofitting this mid-feature was cheap because the VM only
  depended on the interface shape.
- **Test the routing, not the debounce.** Per the `runTest`-real-time gotcha, the
  VM exposes `saveNow()` and the tests drive it directly; the ~2s debounce
  `delay()` is never timed in a test.

## API / Tech Surprises
- **`android.util.Log` throws in plain JVM tests** ("not mocked"). The offline
  save-fallback path logs the failure (we must keep the log per the fix-upload
  MEMORY rule about swallowing exceptions), so the catch block never ran and the
  cache write was skipped. Fix: `testOptions { unitTests.isReturnDefaultValues = true }`
  in `app/build.gradle.kts` — the project had never needed it before because no
  prior unit test hit a `Log` call on a non-error path.
- **Undo/Redo icons moved to `Icons.AutoMirrored`** (`Undo`/`Redo`/`ArrowBack`) —
  the non-mirrored versions are deprecated in current Material icons.
- Room DAO real-SQLite behavior still isn't JVM-testable (no Robolectric); the
  `FakePendingEditDao` locks in the REPLACE-by-PK contract the VM relies on.

## Patterns Worth Reusing
- **Reactive connection gate via `SessionState.connected: StateFlow<Boolean>`**,
  flipped by `JschSftpClient` on connect/disconnect. A VM `collect`s it both to
  grey out UI and to trigger work on the `false → true` (reconnect) transition —
  no NavHost, no network callbacks.
- **Cache-preferred open**: on opening a file, prefer a persisted `PendingEdit`
  over the remote read so unsynced offline work is never clobbered by stale
  server content.
- **Two-mode single screen**: one `@Composable` switches between a list view and
  a detail/editor pane off a single `uiState.openLocation`, avoiding a NavHost in
  the tab-based shell.
