# Editor First-Open Race — Specification

## Bug Description
After connecting and browsing, the first attempt to open a text file via **Files →
long-press → Edit** lands on the Editor tab with the **locations list instead of
the file editor** ("No saved locations yet"). The file never opens; the user must
disconnect and reconnect, then repeat the flow, before the file opens.

Confirmed on-device (019 fix included): the Edit menu does auto-navigate to the
Editor tab (so `editFile` runs and the path is set), and a plain retry **without**
disconnecting still fails — only a disconnect+reconnect recovers.

## Root Cause (revised — v2/v3)

### v2 — the handoff (file never opened)
The transient-open **handoff** was fire-and-forget and order-dependent:

1. `FilesViewModel.editFile()` wrote the path to a plain `@Volatile var
   SessionState.pendingEditPath` and emitted a `navigateToEditor` SharedFlow event.
2. `EditorScreen` consumed the path via `LaunchedEffect(Unit) {
   viewModel.consumePendingEdit() }` — the open only happened if the Editor screen
   (re)entered composition at the right moment.
3. On-device the file never opened (Editor showed the locations list) and plain
   retries failed until a disconnect+reconnect forced a fresh re-entry.

Fixed by making the bridge reactive: `pendingEditPath` is a `StateFlow<String?>`
collected in the `EditorViewModel` init — the open fires on the emission itself
(replay covers "VM created after the path was set", push covers "VM exists"), no
composition timing involved.

### v3 — the locked editor (file opens but not editable)
After the reactive bridge, the file **opens** but the field stays read-only. The
editor's `editable` gate and `open()` decision both depended on
`SessionState.connected` — a manual flag that can disagree with the real session:

- The JSch session can **silently die** during browsing (mobile NAT/proxy idle
  drops; keepalive was 30 s — too slow). The next SFTP operation (the editor's
  read) hits 016's dead-session detection, flips `connected=false`, and the open
  lands in `NotConnected` → field locked. Reconnect is the only escape.
- `editable` additionally required `loaded` (v1), so **any** failed load (even a
  transient read error while connected) locked the field.

Fixed by:
1. **Keepalive tuning** — `setServerAliveInterval(10_000)` +
   `setServerAliveCountMax(3)` in `connect()` keeps mobile NATs from dropping the
   idle session and detects dead ones faster.
2. **Trust the operation, not the flag** — `open()` no longer gates on
   `connected`; it always attempts `readText` and maps the outcome
   (`IllegalStateException` → NotConnected, `SftpException` → Error). A stale
   `connected` can no longer lock the editor (stale-false) or crash it
   (stale-true).
3. **`editable = openLocation != null && loaded`** — a file with content loaded is
   editable even while `connected` is stale-false (edits are cached for sync, the
   014 offline flow); a failed load stays locked with its status visible.

The v1 fixes (reconnect self-heal, `IllegalStateException` catch, `loaded` flag)
remain and are complementary.

## Fix (v2 — reactive bridge)

1. **`sftp/SessionState.kt`**: `pendingEditPath` becomes a
   `MutableStateFlow<String?>` (private) exposed as `StateFlow` with
   `setPendingEdit(path)` / `clearPendingEdit()` — mirroring the existing
   `connected` pattern. Thread-safe; replays the current value to new
   subscribers and pushes updates.
2. **`ui/files/FilesViewModel.kt`** `editFile()`: `sessionState.setPendingEdit(
   file.path)`; the `navigateToEditor` emit stays (tab switch only).
3. **`ui/editor/EditorViewModel.kt`**: replace `consumePendingEdit()` and the
   `LaunchedEffect(Unit)` consume in `EditorScreen` with an **init collect** on
   `sessionState.pendingEditPath`: on a non-null path → `clearPendingEdit()` →
   `open(EditorLocation.of(path))`. The EditorVM opens the file the moment the
   path is set — no screen re-entry, no frame timing.

### v4 — first-tap fallback
Device round 4: the reactive bridge opens on the **second** Edit tap but the
**first** tap still showed the locations list — the VM is created mid-flow on the
first tap and its init collect did not reliably process the replayed value.
Fix: keep the collect **and** restore a screen-entry consume
(`consumePendingEditIfAny()` from `LaunchedEffect(Unit)` in `EditorScreen`) as a
fallback. Consume-and-clear makes the two mechanisms idempotent — the handed
path is opened exactly once regardless of which fires first or whether the
collect's replay was processed.

## Fix (v3 — session health + flag independence)

1. **`sftp/JschSftpClient.kt`**: keepalive 30 s → 10 s, `setServerAliveCountMax(3)`;
   log the dead-session detection.
2. **`ui/editor/EditorViewModel.kt`**: `open()` attempts the read unconditionally
   (outcome = truth); `editable = openLocation != null && loaded`.

## Acceptance Criteria
- [ ] Fresh connect → Files → long-press → Edit opens the file with content and an
      editable field on the **first** tap, every time — and on retries.
- [ ] The open works whether the EditorViewModel already exists (collect push) or
      is created at the first tab switch (collect replay or entry-consume
      fallback) — the handed path is opened exactly once.
- [ ] A file that opened and loaded stays editable through brief connection
      hiccups; genuinely dead sessions show `NotConnected` and reload on reconnect
      (v1 self-heal).
- [ ] Existing editor behaviors (offline-cache saves, cache-first open,
      permission-denied save) are unchanged.

## Out of Scope
- Auto-reconnect on dead session (deferred in 016).
- Changing `JschSftpClient.openChannel()`'s exception type.
- `navigateToEditor` SharedFlow hardening (replay=1).
