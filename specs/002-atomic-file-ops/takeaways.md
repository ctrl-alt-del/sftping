# Atomic File Operations — Takeaways

## What Went Well
- The SAF `OpenDocument` + `CreateDocument` contracts integrate cleanly with Compose via
  `rememberLauncherForActivityResult`. Upload = pick (OpenDocument), download = create (CreateDocument).
- TransferManager as a simple `StateFlow` holder works well for foreground-only 002; wrapping it
  in a thin `@HiltViewModel TransfersViewModel` keeps Compose composables clean without
  requiring custom injection.
- `combinedClickable` handles both tap (enter folder / select in multi-select mode) and long-press
  (toggle selection) in a single modifier — simple and idiomatic.

## What We Learned
- `rememberLauncherForActivityResult` closures capture **by reference at compose time**, not at
  launch time. Any mutable state referenced inside must be **declared before** the launcher,
  otherwise the compose compiler sees an uninitialized variable.
- `withContext(Dispatchers.IO)` is a `suspend` function. A helper function that uses it must be
  `suspend` — a plain `fun` with a `withContext` body will not compile.
- ⚡ **Cache file lifecycle**: files written by the ViewModel (from SAF) and consumed by a
  background Worker must use **identical naming conventions** on both sides. The ViewModel
  copies SAF → temp cache, enqueues the Worker with the task ID, then **renames** the cache
  to match the Worker's expected path (`sftping_ul_<taskId>_<fileName>`). The Worker computes
  the same path from `task.id` + `task.fileName`.
- ⚡ **Do not delete cache before Worker runs**: `cacheFile.delete()` must not happen in the
  ViewModel after `enqueue()` — the Worker hasn't started yet and will find the file missing.
  The Worker owns the cache lifecycle and deletes the file after successful transfer.
- **Asynchronous completion observation**: both upload and download must observe
  `TransferManager.items` for `COMPLETED` status on the task ID before proceeding with
  post-transfer actions (refreshing the file list, copying download result to SAF). Never
  assume the transfer completes synchronously after `enqueue()`.
- `FloatingActionButton` with the speed-dial pattern works, but the `ExtendedFloatingActionButton`
  text changes between "Add" / "Close" depending on expanded state — the expand/collapse toggle
  feels natural on Android.

## API / Tech Surprises
- SAF `ContentResolver.query` for `_display_name` is fragile — some content providers return null.
  Fall back to `uri.lastPathSegment` as a safe default.
- `ChannelSftp.get()` and `put()` with a custom `SftpProgressMonitor` count bytes correctly but
  the monitor's `init(max)` sometimes receives 0 (unknown size) for SFTP servers that don't
  report file size before transfer. The UI handles this gracefully by showing percentage only
  when `totalBytes > 0`.

## Patterns Worth Reusing
- **TransferManager + TransfersViewModel** pattern: singleton state holder (TransferManager)
  paired with a thin `@HiltViewModel` wrapper that exposes the `StateFlow` to Compose.
  This pattern scales naturally to Room-backed persistence in 003.
- **FAB speed-dial** for file operations: a single `ExtendedFloatingActionButton` toggles
  two `SmallFloatingActionButton`s (upload, new folder). Compact and Material 3 compliant.
- **Multi-select via combinedClickable**: tap = enter/select, long-press = toggle.
  Selection state lives in ViewModel as a `selectedPaths: List<String>`, toggled by
  adding/removing from the list. Contextual app bar replaces the standard one when active.
