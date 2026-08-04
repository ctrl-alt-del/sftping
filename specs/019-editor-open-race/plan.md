---
feature_id: "019"
name: "Editor First-Open Race (stuck read-only / file never opens)"
status: "✅ Done"
depends_on: ["014", "015", "016"]
touches:
  - "ui/editor/EditorViewModel.kt"
  - "ui/editor/EditorScreen.kt"
  - "ui/files/FilesViewModel.kt"
  - "sftp/SessionState.kt"
  - "app/src/test/java/com/example/sftping/ui/editor/EditorViewModelTest.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FilesViewModelTest.kt"
  - "AGENTS.md"
  - "README.md"
  - "MEMORY.md"
created: "2026-08-04"
---

# Editor First-Open Race — Plan

## Approach

Two-layer fix. **v1 (landed first)**: harden `EditorViewModel.open()` — gate on
the authoritative `sessionState.connected.value` instead of the `uiState.connected`
mirror, catch `IllegalStateException` (dead session) → `NotConnected`, add a
`loaded` flag (`editable` requires it), and self-heal on reconnect
(`false → true` re-runs the cache-aware open when stuck `NotConnected` with
nothing loaded). This removed a latent crash and made recovery automatic, but the
reported symptom persisted.

**v2 (this change)**: the file **never opened** (Editor showed the locations
list) because the handoff was fire-and-forget: a plain `pendingEditPath` var +
`LaunchedEffect(Unit) { consumePendingEdit() }` consumed it only when the Editor
screen re-entered composition at exactly the right time. Convert the bridge to a
reactive one:

1. `SessionState.pendingEditPath` → `MutableStateFlow<String?>` + accessor +
   `setPendingEdit`/`clearPendingEdit` (mirrors the `connected` pattern).
2. `FilesViewModel.editFile()` → `sessionState.setPendingEdit(file.path)`;
   the `navigateToEditor` emit stays (tab switch only).
3. `EditorViewModel` init collects the flow and opens + clears on any non-null
   emission; the `LaunchedEffect` consume in `EditorScreen` is removed.

The open now fires on the StateFlow emission itself — replay to new subscribers
covers "VM created after the path was set", push covers "VM already exists" — no
composition timing involved, so plain retries work and the disconnect/reconnect
ritual is unnecessary.

## Files Changed

| Action | File | Rationale |
|--------|------|-----------|
| Change | `sftp/SessionState.kt` | `pendingEditPath` plain var → `StateFlow<String?>` + setter/clearer |
| Change | `ui/files/FilesViewModel.kt` | `editFile()` writes via `setPendingEdit` |
| Change | `ui/editor/EditorViewModel.kt` | Init collect opens+clears handed paths; `consumePendingEdit()` removed |
| Change | `ui/editor/EditorScreen.kt` | Remove the `LaunchedEffect(Unit)` consume |
| Change | tests | StateFlow API updates; new handoff coverage (path set before/after VM creation; full FilesVM→EditorVM handoff) |
| Change | `specs/index.md`, `MEMORY.md`, `AGENTS.md`, `README.md` | v2 findings |

## Risks

- **Double-open**: `open()` may run for a handed path while another file is open
  — same as the old consume-on-entry behavior; `open()` replaces `openLocation`.
- **Stale StateFlow value**: cleared after each open, so a later Edit tap opens
  the newest path only.
- **VM scope**: the collect runs in `viewModelScope` (same lifetime as the old
  path); if the Activity dies, everything resets as before.

## Dependencies

014 (editor connected gate / offline cache), 015 (Files → Editor handoff), 016
(`connected` StateFlow accuracy).
