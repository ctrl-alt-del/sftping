---
feature_id: "014"
name: "Remote File Editor"
status: "✅ Done"
depends_on: ["001"]
touches:
  - "app/src/main/java/com/example/sftping/MainActivity.kt"
  - "app/src/main/java/com/example/sftping/sftp/ISftpClient.kt"
  - "app/src/main/java/com/example/sftping/sftp/JschSftpClient.kt"
  - "app/src/main/java/com/example/sftping/sftp/SessionState.kt"
  - "app/src/main/java/com/example/sftping/data/editor/EditorLocation.kt"
  - "app/src/main/java/com/example/sftping/data/editor/EditorLocationRepository.kt"
  - "app/src/main/java/com/example/sftping/data/editor/PendingEdit.kt"
  - "app/src/main/java/com/example/sftping/data/editor/PendingEditDao.kt"
  - "app/src/main/java/com/example/sftping/data/editor/EditorDatabase.kt"
  - "app/src/main/java/com/example/sftping/di/EditorModule.kt"
  - "app/src/main/java/com/example/sftping/ui/editor/UndoStack.kt"
  - "app/src/main/java/com/example/sftping/ui/editor/EditorViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/editor/EditorScreen.kt"
  - "app/src/test/java/com/example/sftping/ui/editor/UndoStackTest.kt"
  - "app/src/test/java/com/example/sftping/data/editor/EditorLocationTest.kt"
  - "app/src/test/java/com/example/sftping/data/editor/PendingEditDaoTest.kt"
  - "app/src/test/java/com/example/sftping/ui/editor/EditorViewModelTest.kt"
  - "AGENTS.md"
  - "README.md"
created: "2026-07-12"
---

# Remote File Editor — Plan

## Approach

A new **Editor** tab is added to `MainActivity`'s `AppDestinations` enum + `when`
branch (no NavHost; an Activity-scoped `viewModel()` works with `@HiltViewModel`
per the established shell pattern). The tab is state-driven and always accessible.

**Connection status** becomes observable: `SessionState` gains a
`connected: StateFlow<Boolean>` that `JschSftpClient` flips `true` after a
successful `connect()` and `false` on `disconnect()`. The Editor greys out its
text field and disables Undo/Redo/Save when `connected == false`, and observes
`false → true` transitions to trigger re-sync of the open file's pending edits.
(Locations management stays enabled offline.)

**Text I/O** is added to `ISftpClient`: `readText(path): String` and
`writeText(path, content)`, implemented in `JschSftpClient` with a **fresh
`ChannelSftp` per operation** over the shared session (`get`/`put` streams, UTF-8),
consistent with the per-operation channel concurrency fix.

**Saved locations** are modeled by `EditorLocation(id, label, remotePath, addedAt)`
persisted as a JSON list under one DataStore key via `EditorLocationRepository`
(mirrors `ConnectionProfile`/`TrustedHost`; `org.json` helpers are JVM-testable).

**Offline cache** lives in a **separate** Room DB `sftping_editor.db` with a
`PendingEdit(remotePath PK, content, updatedAt)` entity + DAO (keeps the transfer
DB untouched — no risky migration). Autosave writes to the remote when connected;
on failure/offline it upserts a `PendingEdit` and flags the location as pending.
Opening a location prefers a `PendingEdit` over the remote read so unsynced work
is never lost.

**EditorViewModel** owns: locations CRUD, open/load (cache-aware), a debounced
autosave (~2s) that routes online→remote / offline→cache, a manual save that
flushes immediately, reconnect-driven flush (collects `SessionState.connected`),
and undo/redo backed by a pure `UndoStack`.

**UndoStack** is a pure, JVM-testable class: a snapshot list + cursor with
`push`, `undo`, `redo`, `canUndo`, `canRedo`, and redo-branch truncation on a new
push. Toolbar icons bind to `canUndo`/`canRedo`.

Pure logic (UndoStack, EditorLocation JSON, PendingEditDao, cache-vs-remote and
online/offline routing in the VM using a fake client + in-memory stores) is
unit-tested. SFTP stream I/O and Compose UI are covered by build + manual testing,
consistent with existing untested I/O code.

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Edit | `sftp/SessionState.kt` | add `connected: StateFlow<Boolean>` + setter |
| Edit | `sftp/ISftpClient.kt` | add `readText` / `writeText` |
| Edit | `sftp/JschSftpClient.kt` | implement text I/O (fresh channel, UTF-8); flip `connected` |
| Create | `data/editor/EditorLocation.kt` | model + `org.json` list helpers |
| Create | `data/editor/EditorLocationRepository.kt` | DataStore-backed CRUD |
| Create | `data/editor/PendingEdit.kt` | Room entity (remotePath PK) |
| Create | `data/editor/PendingEditDao.kt` | upsert/get/delete/flow |
| Create | `data/editor/EditorDatabase.kt` | `sftping_editor.db` (v1) |
| Create | `di/EditorModule.kt` | provide EditorDatabase + DAO |
| Create | `ui/editor/UndoStack.kt` | pure undo/redo history |
| Create | `ui/editor/EditorViewModel.kt` | `@HiltViewModel`: CRUD, autosave, offline, reconnect, undo/redo |
| Create | `ui/editor/EditorScreen.kt` | Compose: locations list + editor pane + toolbar |
| Edit | `MainActivity.kt` | add `EDITOR` destination + `when` branch |
| Create | tests (UndoStack, EditorLocation, PendingEditDao, EditorViewModel) | pure-logic + DAO coverage |
| Edit | `AGENTS.md`, `README.md` | reflect 4th tab, package map, known-gaps |

## Risks
- SFTP stream I/O and Compose aren't JVM-testable — keep them thin; cover routing
  and history logic in pure helpers/VM tests with fakes.
- Reconnect flush depends on the `connected` flag being flipped correctly by the
  client; unit-test the VM against a fake `SessionState`/client, not JSch.
- Debounced autosave on real dispatchers is race-prone in tests (mem: `runTest`
  real-time) — test routing decisions synchronously (e.g. `saveNow`) rather than
  timing the debounce.
- Separate DB avoids a transfer-DB migration; the tradeoff is a second Room
  instance (acceptable, isolated).

## Dependencies
- 001 (SFTP session, `ISftpClient`, connection shell, `SessionState`).

## ADR-013
Offline editor edits are persisted in a **separate** Room database
(`sftping_editor.db`) rather than adding a table to the transfer DB, to avoid a
schema migration on the transfer store and keep editor state fully isolated.
Saved locations use DataStore + JSON (public, list-shaped) while pending edit
**content** uses Room (larger, upsert-by-key, survives process death).

## ADR-014
Connection state is exposed as a `StateFlow<Boolean>` on the singleton
`SessionState`, flipped by `JschSftpClient` on connect/disconnect. This gives the
Editor a reactive gate (grey-out) and a re-sync trigger without introducing a
NavHost or a new connectivity subsystem. Re-sync is triggered on the
`false → true` transition of this flag (i.e., the user reconnecting via the
Connect tab), not by background network callbacks.
