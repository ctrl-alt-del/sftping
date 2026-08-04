# Editor First-Open Race — Test Plan

## Unit Tests (`EditorViewModelTest.kt`)

### New
- [x] **Reconnect reloads a file that opened while disconnected**: open while
      `connected == false` → `NotConnected`, read-only; `setConnected(true)` →
      content loads from the fake client, `editable == true`, status `Idle`.
- [x] **Reconnect keeps in-memory edits of an already loaded file**: open connected,
      edit (autosave flushed while connected), disconnect → `NotConnected`;
      reconnect → content still the edited value, `editable == true` (no reload, no
      clobber — `loaded` guard).
- [x] **Open with dead session maps to NotConnected instead of crashing**: fake
      `readText` throws `IllegalStateException("Not connected")` while the
      `connected` StateFlow is `true`; open → status `NotConnected`, `editable == false`.

### Existing (must remain green)
- [x] `open while disconnected is read-only and does not read remote` — authoritative
      gate `false` → `NotConnected`, `readCount == 0`.
- [x] `consumePendingEdit opens the handed path transiently and clears it` — handoff
      unchanged; open uses the authoritative `true` → reads.
- [x] `reconnect flushes pending edit to server` — pending-edit flush branch of
      `onConnectedChanged` unchanged.
- [x] `open prefers pending cached content over remote` / `saveNow online/offline` /
      permission-denied save / undo-redo — unaffected.

## Integration / Manual
- Connect → Files → long-press `.txt` → **Edit**: file opens with content and is
  editable on the **first** attempt (no disconnect needed).
- Open a file, disconnect, reconnect: file content remains and the field unlocks.
- Open while truly offline (session down): shows "Not connected" status, read-only;
  after reconnect the file loads automatically.
