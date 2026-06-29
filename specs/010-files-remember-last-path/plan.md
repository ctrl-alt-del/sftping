---
feature_id: "010"
name: "Files Page: Remember Last Visited Path"
status: "✅ Done"
depends_on: ["008"]
touches:
  - "app/src/main/java/com/example/sftping/sftp/SessionState.kt"
  - "app/src/main/java/com/example/sftping/ui/connection/ConnectionViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesScreen.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FilesViewModelTest.kt"
  - "app/src/test/java/com/example/sftping/ui/connection/ConnectionViewModelTest.kt"
created: "2026-06-30"
---

# Files Page: Remember Last Visited Path — Plan

## Approach

`FilesViewModel` is Activity-scoped (`viewModel()`), so its `currentPath` /
`pathStack` already survive tab switches. The only thing discarding the path is
`FilesScreen`'s `LaunchedEffect(Unit) { viewModel.loadFiles() }`, which re-fires
on every re-entry and defaults to `sessionState.initialDirectory`.

Replace that with `viewModel.onEnterScreen()`, which distinguishes a **new
connection** from **returning to the tab** using a per-connection epoch:

- Add `epoch` to `SessionState`; `ConnectionViewModel.onConnected` increments it
  on each successful connect (alongside setting `initialDirectory`).
- `FilesViewModel` remembers the `loadedEpoch`. On enter: if the epoch changed →
  new session → reset `pathStack`/`searchQuery` and load `initialDirectory`
  (preserves 008); otherwise → reload the last `currentPath` (the remembered path).

This preserves feature 008 (first entry / reconnect → home) and 009 (sort/hidden
persist), and adds last-path memory. In-memory only.

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Edit | `sftp/SessionState.kt` | add `epoch` (connection session counter) |
| Edit | `ui/connection/ConnectionViewModel.kt` | bump `epoch` in `onConnected` |
| Edit | `ui/files/FilesViewModel.kt` | `onEnterScreen()` + `loadedEpoch` |
| Edit | `ui/files/FilesScreen.kt` | call `onEnterScreen()` from `LaunchedEffect` |
| Edit | tests (Files VM, Connection VM) | epoch + onEnterScreen behavior |

## Risks
- Must keep 008's "open home on connect" — covered by the epoch reset branch and
  by `ConnectionViewModelTest` continuing to assert `initialDirectory`.

## Dependencies
- 008 (Default Directory) — owns `SessionState` + `onConnected` initial-dir logic.

## ADR-009
Detect a new connection with a `SessionState.epoch` bumped on each connect, rather
than comparing `initialDirectory` (which can repeat across hosts). `FilesViewModel`
reloads the last `currentPath` on same-session re-entry and resets to
`initialDirectory` on epoch change.
