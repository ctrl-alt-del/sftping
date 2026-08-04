# Editor First-Open Race — Specification

## Bug Description
After connecting and browsing, the first attempt to open a `.txt` file via **Files →
long-press → Edit** lands on the Editor tab with a **read-only, empty editor**
("cannot edit the file"). The user must disconnect and reconnect, then repeat the
flow once or twice, before the file loads and becomes editable.

## Reproduction
1. Connect to a remote host on the Connect tab.
2. Go to the Files tab, long-press a text file, tap **Edit**.
3. The Editor tab opens the file pane, but the field is read-only and empty
   (`SaveStatus.NotConnected`), even though the session is live (browsing works).

## Root Cause
A one-shot, non-recoverable open decision based on a **derived snapshot** of
connection state:

1. `EditorViewModel.open()` decides between "read the file" and "read-only
   `NotConnected`" by checking `!uiState.connected` — a VM-local copy of
   `SessionState.connected` fed by the collect coroutine started in `init`.
2. That check runs **asynchronously inside a coroutine, after a Room suspension**
   (`pendingEditDao.get()`), so nothing orders the collect's delivery against the
   decision read.
3. If the check reads `false` on first Editor entry (the collect emission not yet
   applied to `uiState`), `open()` silently takes the `NotConnected` branch: the
   file "opens" with empty content and `readOnly = !editable`. There is **no retry
   path**: `open()` never re-checks, and `onConnectedChanged()` only flushed
   *pending edits* on reconnect — it never reloaded a stuck open.
4. The only escape was a reconnect, which re-fires `SessionState.connected`
   `false → true`, re-emits to the (by then existing) ViewModel, and lets the next
   `consumePendingEdit()`/`open()` see `true` — exactly the reported workaround.

### Latent crash in the same path
`open()` catches only `SftpException`, but `JschSftpClient.openChannel()` throws a
raw `IllegalStateException("Not connected")` when the session is down. If the
snapshot is stale-`true` while the session actually died, `readText()` throws
uncaught → crash instead of a graceful NotConnected state.

## Fix
Three small changes, all in `ui/editor/EditorViewModel.kt`:

1. **Decide with the authoritative source.** `open()` gates on
   `sessionState.connected.value` (a `StateFlow.value` read is always current and
   thread-safe) instead of the `uiState.connected` mirror. The mirror remains for
   UI display only.
2. **Crash-proof the read.** `open()` also catches `IllegalStateException`
   (dead session between the check and the read) and maps it to `NotConnected`.
3. **Self-heal on reconnect.** `onConnectedChanged()` — on the `false → true`
   transition, after the existing pending-edit flush — re-runs the cache-aware
   `open()` when the open file is stuck in `NotConnected` with nothing loaded.
   The manual "disconnect and retry" workaround becomes automatic.

### Why a `loaded` flag
The recovery must not clobber in-memory edits of an already-loaded file when a
session drops and reconnects (the offline-editing flow from 014). A new
`EditorUiState.loaded` flag is `true` once content was materialized (cache branch
or successful remote read) and `false` in the NotConnected/Error branches. The
recovery fires only when `!loaded && saveStatus is NotConnected`.

`editable` additionally requires `loaded`, so a failed load (dead session, read
error) leaves the field locked — consistent with its "Not connected" status —
until the recovery (reconnect) reloads it.

## Acceptance Criteria
- [ ] Fresh connect → Files → long-press → Edit opens the file with content and an
      editable field on the first attempt.
- [ ] If the first open lands in `NotConnected` (session down), reconnecting from
      the Connect tab reloads the file automatically — no manual re-tap of Edit.
- [ ] In-memory edits of an already-loaded file survive a disconnect → reconnect
      (field unlocks, content untouched, no reload).
- [ ] Opening with a dead session shows `NotConnected` (no crash).
- [ ] Existing editor behaviors (offline read-only open, cache-first open, pending
      edit flush on reconnect, permission-denied save) are unchanged.

## Out of Scope
- Hardening the Files → Editor handoff (`SessionState.pendingEditPath` +
  `LaunchedEffect` consume) — it is fragile but demonstrably working (the user
  reaches the Editor); noted as a future improvement.
- Changing `JschSftpClient.openChannel()`'s exception type (`IllegalStateException`
  vs `SftpException`) — kept as-is to stay minimal; the Editor now handles it.
