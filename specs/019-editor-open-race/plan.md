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
  - "sftp/JschSftpClient.kt"
  - "app/src/test/java/com/example/sftping/ui/editor/EditorViewModelTest.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FilesViewModelTest.kt"
  - "AGENTS.md"
  - "README.md"
  - "MEMORY.md"
created: "2026-08-04"
---

# Editor First-Open Race — Plan

## Approach

Three-layer fix, each informed by a device report:

- **v1 (landed first)**: harden `EditorViewModel.open()` — authoritative
  `connected` read, catch `IllegalStateException` (dead session) → `NotConnected`,
  add a `loaded` flag, reconnect self-heal. Correct hardening, but not the bug.
- **v2**: the file never opened (Editor showed the locations list) — the handoff
  was a plain `pendingEditPath` var consumed by a `LaunchedEffect(Unit)` on Editor
  re-entry (order-dependent). Convert to a `StateFlow<String?>` + EditorVM init
  collect; removed `consumePendingEdit()`/`LaunchedEffect`.
- **v3**: the file opens but stays read-only. The `editable` gate and `open()`
  decision trusted the manual `connected` flag, which can disagree with the real
  session (mobile NAT idle-drops kill the session during browsing — keepalive was
  30 s). Fix: keepalive 10 s / countMax 3; `open()` attempts the read
  unconditionally (the outcome is the truth); `editable = openLocation != null &&
  loaded`.

## Files Changed

| Action | File | Rationale |
|--------|------|-----------|
| Change | `sftp/SessionState.kt` | `pendingEditPath` plain var → `StateFlow<String?>` + setter/clearer (v2) |
| Change | `ui/files/FilesViewModel.kt` | `editFile()` writes via `setPendingEdit` (v2) |
| Change | `ui/editor/EditorViewModel.kt` | Init collect opens+clears handed paths (v2); unconditional read + `editable` = open+loaded (v3) |
| Change | `ui/editor/EditorScreen.kt` | Remove the `LaunchedEffect(Unit)` consume (v2) |
| Change | `sftp/JschSftpClient.kt` | Keepalive 10 s + `setServerAliveCountMax(3)`; log dead-session detection (v3) |
| Change | tests | StateFlow API updates; handoff + dead-session coverage |
| Change | `specs/index.md`, `MEMORY.md`, `AGENTS.md`, `README.md` | v2/v3 findings |

## Risks

- **Keepalive traffic**: 10 s keepalives add a tiny heartbeat packet every 10 s per
  connection — negligible; standard practice for SFTP/SSH clients on mobile.
- **Unconditional read on offline open**: fast-fails via `IllegalStateException`
  → `NotConnected` (same UX as the old gate, minus the flag dependency).
- **Offline editing of a loaded file**: now allowed (`editable` drops the
  `connected` requirement) — edits cache for sync (014's offline flow), consistent
  with the design.

## Dependencies

014 (editor connected gate / offline cache), 015 (Files → Editor handoff), 016
(`connected` StateFlow accuracy / dead-session detection).
