# Remote File Editor — Test Plan

## Unit Tests (JVM — `./gradlew testDebug`)

### UndoStack (`ui/editor/UndoStackTest.kt`)
- [ ] **initial state**: Arrange `UndoStack("hello")`; Assert `current == "hello"`,
  `canUndo == false`, `canRedo == false`.
- [ ] **push then undo**: Arrange stack at "a"; Act `push("ab")`, `undo()`;
  Assert `current == "a"`, `canRedo == true`.
- [ ] **undo then redo**: Act `push("ab")`, `undo()`, `redo()`; Assert
  `current == "ab"`, `canRedo == false`.
- [ ] **undo at head is no-op**: Act `undo()` on fresh stack; Assert unchanged and
  `canUndo == false`.
- [ ] **redo at tail is no-op**: Act `redo()` with nothing ahead; Assert unchanged.
- [ ] **new push truncates redo branch**: Act `push("ab")`, `undo()`, `push("ac")`;
  Assert `current == "ac"`, `canRedo == false`.
- [ ] **duplicate push is ignored**: Act `push("a")` when current is already "a";
  Assert history length unchanged, `canUndo == false`.

### EditorLocation JSON (`data/editor/EditorLocationTest.kt`)
- [ ] **round-trip**: Arrange a `List<EditorLocation>`; Act `listToJson` →
  `listFromJson`; Assert equal (id, label, remotePath, addedAt preserved).
- [ ] **empty / blank json**: `listFromJson("[]")` and `listFromJson("")` → empty
  list (no crash).
- [ ] **label defaults to file name** when blank via `EditorLocation.of(...)`
  helper: path `/etc/nginx/nginx.conf`, blank label → label `nginx.conf`.

### PendingEditDao (`data/editor/PendingEditDaoTest.kt`, in-memory Room)
- [ ] **upsert + get**: Act `upsert(PendingEdit(...))`; Assert `get(path)` returns it.
- [ ] **upsert replaces by remotePath**: two upserts same path → latest content wins,
  single row.
- [ ] **delete**: Act `delete(path)`; Assert `get(path) == null`.
- [ ] **pendingPaths flow / all**: two rows → returns both paths.
> Use named parameters when constructing entities (Room KSP default-arg gotcha).

### EditorViewModel (`ui/editor/EditorViewModelTest.kt`, fakes)
Uses a fake `ISftpClient`, in-memory location repo, in-memory `PendingEditDao`,
and a controllable `SessionState`.
- [ ] **addLocation persists + lists**: Act `addLocation(label, path)`; Assert it
  appears in `uiState.locations` and repo.
- [ ] **editLocation / deleteLocation**: update fields / remove; deleting also
  clears its pending edit.
- [ ] **open while connected loads remote text**: fake client returns "remote";
  Act `open(loc)`; Assert `uiState.content == "remote"`, editable.
- [ ] **open prefers pending cache**: seed `PendingEdit(path,"cached")`; Act
  `open(loc)`; Assert `content == "cached"`, `pending == true`.
- [ ] **open while disconnected**: `connected=false`; Assert editor read-only,
  status "Not connected", no client read attempted.
- [ ] **saveNow online writes remote + clears pending**: Act edit + `saveNow()`;
  Assert fake client received `writeText(path, content)`, no pending row.
- [ ] **saveNow offline caches + flags pending**: `connected=false` (or client
  throws); Act `saveNow()`; Assert `PendingEdit` upserted, status pending.
- [ ] **reconnect flushes pending**: open offline with pending → flip
  `connected` true; Assert `writeText` called and pending cleared.
- [ ] **undo/redo drive content + flags**: Act edits, `undo()`, `redo()`; Assert
  `content` and `canUndo`/`canRedo` track `UndoStack`.
- [ ] **open non-existent path surfaces error**: fake client throws on read; Assert
  `uiState.error` set, read-only.

## Integration / UI (manual)
- [ ] Editor tab shows as 4th tab; opens locations list.
- [ ] Add/edit/delete a location; persists across app restart.
- [ ] Connected: open file → edit → autosave shows Saving…→Saved.
- [ ] Save icon flushes immediately; Undo/Redo icons enable/disable correctly.
- [ ] Turn off network mid-edit → status flips to cached/pending, editor still shows text.
- [ ] Reconnect via Connect tab → pending edits flush to server.
- [ ] Disconnected: editor greyed/read-only, toolbar disabled.

## Edge Cases
- [ ] Blank remote path → Add/Save disabled.
- [ ] Undo at head / redo at tail → no-op (icons disabled).
- [ ] New edit after undo truncates redo branch.
- [ ] Delete a location with a pending edit → pending edit removed too.
- [ ] Session drop mid-edit → pane transitions to disconnected without crash.
