---
feature_id: "019"
name: "Editor First-Open Race (stuck read-only editor)"
status: "✅ Done"
depends_on: ["014", "015", "016"]
touches:
  - "ui/editor/EditorViewModel.kt"
  - "app/src/test/java/com/example/sftping/ui/editor/EditorViewModelTest.kt"
  - "AGENTS.md"
  - "README.md"
  - "MEMORY.md"
created: "2026-08-04"
---

# Editor First-Open Race — Plan

## Approach

Fix the editor's open decision and make it self-correcting, entirely in
`ui/editor/EditorViewModel.kt` — no protocol/session changes, no UI changes.

1. **Authoritative gate**: `open()` checks `sessionState.connected.value` instead
   of the `uiState.connected` mirror, removing the collect-delivery race at the
   decision point. `StateFlow.value` is always current (thread-safe, lock-free).
2. **Crash-proof read**: catch `IllegalStateException` (thrown by
   `JschSftpClient.openChannel()` for a dead/null session) in `open()` and map it
   to `SaveStatus.NotConnected` — previously an uncaught crash when the snapshot
   said connected but the session had died.
3. **Self-healing recovery**: `onConnectedChanged()` re-runs the cache-aware
   `open()` on `false → true` when the open file is stuck in `NotConnected` with
   nothing loaded (`!loaded`), making the reported "disconnect and retry"
   workaround automatic.
4. **`loaded` flag**: `EditorUiState.loaded` tracks whether content was ever
   materialized (cache or remote). It guards the recovery against clobbering
   in-memory edits of a loaded file after a drop/reconnect (014 offline flow), and
   `editable` now requires it so a failed load stays read-only.

## Files Changed

| Action | File | Rationale |
|--------|------|-----------|
| Change | `ui/editor/EditorViewModel.kt` | Authoritative gate, `IllegalStateException` catch, `loaded` flag, reconnect recovery, `editable` gate |
| Change | `app/src/test/.../EditorViewModelTest.kt` | 3 new tests (reconnect recovery, edit-preservation on reconnect, dead-session open); existing tests unchanged/passing |
| Change | `specs/index.md`, `MEMORY.md`, `AGENTS.md`, `README.md` | 019 row, takeaways promotion, known-gap note |

## Risks

- **Recovery clobbering unsaved work**: guarded by `loaded` — recovery only fires
  when nothing was ever loaded; loaded files keep their in-memory content on
  reconnect (field unlocks via `connected`).
- **Re-entrancy**: `onConnectedChanged` → `open()` launches a fresh coroutine;
  no nested state mutation on the collecting coroutine's stack.
- **Behavior change**: `editable` now requires `loaded` — a file whose read failed
  (e.g. missing file) stays locked instead of allowing edits into an empty buffer.
  Consistent with the edit-existing-only known gap; acceptable.

## Dependencies

014 (editor connected gate / offline cache), 015 (Files → Editor handoff), 016
(`connected` StateFlow accuracy / dead-session detection).
