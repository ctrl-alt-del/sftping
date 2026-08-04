# Editor First-Open Race — Test Plan

## Unit Tests

### EditorViewModelTest.kt

#### v1 — open() hardening
- [x] **Reconnect reloads a file that opened while disconnected**: open while
      `connected == false` → `NotConnected`, read-only; `setConnected(true)` →
      content loads, `editable == true`, status `Idle`.
- [x] **Reconnect keeps in-memory edits of an already loaded file**: loaded →
      edit (autosave flushed) → disconnect → reconnect → content preserved
      (`loaded` guard), `editable == true`.
- [x] **Open with dead session maps to NotConnected instead of crashing**: fake
      `readText` throws `IllegalStateException` → `NotConnected`, no crash.

#### v2 — reactive handoff
- [x] **Handed edit path set before VM creation opens transiently and clears it**:
      `session.setPendingEdit(path)` → `vm()` → content loads from the fake
      client, `openLocation` set, path cleared, not persisted to locations.
- [x] **Handed edit path set after VM creation opens transiently and clears it**:
      `vm()` first (no open), then `session.setPendingEdit(path)` → collect
      delivers the push → opens + clears.
- [x] **No pending edit path leaves editor on the locations list**: no path →
      `openLocation == null`.

#### v3 — flag-independent open + session health
- [x] **Open with dead session while disconnected is read-only NotConnected**:
      fake `readText` throws `IllegalStateException` → `NotConnected`,
      `editable == false` (nothing loaded).
- [x] **Reconnect reloads a file that opened with a dead session**: dead-session
      open → `NotConnected`; after `setConnected(true)` (and the session back),
      the v1 self-heal reloads content, `editable == true`, status `Idle`.
- [x] **Open with dead session maps to NotConnected instead of crashing** (v1):
      `IllegalStateException` with a stale-`true` connected flag → `NotConnected`,
      no crash.

### FilesViewModelTest.kt
- [x] **editFile stashes path in sessionState and emits navigate event**:
      `sessionState.pendingEditPath.value == path`, event emitted.
- [x] **Full handoff — editFile hands the path to the editor which opens it
      transiently**: FilesVM `editFile` → EditorVM (constructed alongside)
      opens the file and clears the path.

### Existing (must remain green)
- [x] `open while connected loads remote text`, `open prefers pending cached
      content over remote`, `saveNow online/offline`, permission-denied save,
      undo/redo, reconnect flush, `reconnect keeps in-memory edits of an already
      loaded file` (editable via `loaded`, not `connected`)

## Integration / Manual
- Connect → Files → long-press `.txt` → **Edit**: file opens with content and is
  editable on the **first** attempt — including after a 30–60 s browsing pause
  (keepalive keeps the session alive).
- Without disconnecting: back to Files, long-press the same file → **Edit** again
  — opens on the retry.
- Let the app sit idle > 30 s on the file list, then Edit: the file should still
  open editable (10 s keepalive). If it ever shows "Not connected", reconnect
  reloads it automatically (v1 self-heal).
