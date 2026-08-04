# Editor First-Open Race — Specification

## Bug Description
After connecting and browsing, the first attempt to open a text file via **Files →
long-press → Edit** lands on the Editor tab with the **locations list instead of
the file editor** ("No saved locations yet"). The file never opens; the user must
disconnect and reconnect, then repeat the flow, before the file opens.

Confirmed on-device (019 fix included): the Edit menu does auto-navigate to the
Editor tab (so `editFile` runs and the path is set), and a plain retry **without**
disconnecting still fails — only a disconnect+reconnect recovers.

## Root Cause (revised — v2)

The transient-open **handoff** is fire-and-forget and order-dependent:

1. `FilesViewModel.editFile()` writes the path to a plain `@Volatile var
   SessionState.pendingEditPath` and emits a `navigateToEditor` SharedFlow event.
2. `EditorScreen` consumes the path via `LaunchedEffect(Unit) {
   viewModel.consumePendingEdit() }` — i.e., the open only happens if the
   Editor screen (re)enters composition *after* the path was written and reads
   the var at exactly the right time.
3. On-device, `consumePendingEdit()` does not open the file (the consume runs
   without seeing the path — a composition-frame timing race between the
   LaunchedEffect dispatch and the write/consume). `openLocation` stays `null`,
   so the Editor shows the locations list. Because the LaunchedEffect consume
   and the plain var have no ordering guarantee, the failure persists across
   plain retries; only a disconnect+reconnect (which forces a fresh Editor
   re-entry at a different point) happens to succeed.

The v1 fix (019) hardened `EditorViewModel.open()` — authoritative
`sessionState.connected.value` gate, `IllegalStateException` catch, `loaded`
flag, reconnect self-heal — but did not touch the handoff, so the symptom
persisted. That hardening is correct and stays.

## Fix (v2 — reactive bridge)

Make the handoff reactive and composition-independent:

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

## Acceptance Criteria
- [ ] Fresh connect → Files → long-press → Edit opens the file with content and
      an editable field on the first attempt, every time.
- [ ] A plain retry (Files → Edit again, no disconnect) opens the file.
- [ ] The open works whether the EditorViewModel already exists or is created
      after the path is set (StateFlow replay + push).
- [ ] No disconnect/reconnect ritual needed.
- [ ] Existing editor behaviors (offline read-only open, cache-first open,
      pending-edit flush, permission-denied save, v1 self-heal) are unchanged.

## Out of Scope
- Changing `JschSftpClient.openChannel()`'s exception type — the Editor now
  handles both `SftpException` and `IllegalStateException`.
- `navigateToEditor` SharedFlow hardening (replay=1) — with the reactive bridge,
  a dropped nav event is self-healing (the file opens in the VM regardless).
