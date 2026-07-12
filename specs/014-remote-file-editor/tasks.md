# Remote File Editor — Tasks

One task = one commit that builds + passes tests (+ lint on UI tasks).

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted & approved
- [x] Mockup: `ux-ui/mockups.png`
- [x] test_plan.md documented

## Block 1: Plumbing (connection status + text I/O)
- [ ] **Task 1.1**: `SessionState.connected: StateFlow<Boolean>` + setter;
  `ISftpClient.readText/writeText`; implement in `JschSftpClient` (fresh channel,
  UTF-8) and flip `connected` on connect/disconnect —
  `sftp/SessionState.kt`, `sftp/ISftpClient.kt`, `sftp/JschSftpClient.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: Saved locations persistence
- [ ] **Task 2.1**: `EditorLocation` (+ `of()` helper + `org.json` list helpers)
  and `EditorLocationRepository` (DataStore) + JSON tests —
  `data/editor/EditorLocation.kt`, `data/editor/EditorLocationRepository.kt`,
  `test/.../data/editor/EditorLocationTest.kt`
  - Tests: `./gradlew testDebug`

## Block 3: Offline cache DB
- [ ] **Task 3.1**: `PendingEdit` entity + `PendingEditDao` + `EditorDatabase`
  (`sftping_editor.db`) + `EditorModule` providers + DAO test —
  `data/editor/PendingEdit.kt`, `data/editor/PendingEditDao.kt`,
  `data/editor/EditorDatabase.kt`, `di/EditorModule.kt`,
  `test/.../data/editor/PendingEditDaoTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 4: Undo/redo
- [ ] **Task 4.1**: pure `UndoStack` + exhaustive tests —
  `ui/editor/UndoStack.kt`, `test/.../ui/editor/UndoStackTest.kt`
  - Tests: `./gradlew testDebug`

## Block 5: EditorViewModel
- [ ] **Task 5.1**: `EditorViewModel` — locations CRUD, cache-aware open/load,
  debounced autosave, offline fallback, reconnect flush, undo/redo + tests —
  `ui/editor/EditorViewModel.kt`, `test/.../ui/editor/EditorViewModelTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 6: Editor UI + new tab
- [ ] **Task 6.1**: `EditorScreen` (locations list + add/edit sheet + editor pane
  + toolbar/status bar) and wire `AppDestinations.EDITOR` in `MainActivity` —
  `ui/editor/EditorScreen.kt`, `MainActivity.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 7: Ship
- [ ] **Task 7.1**: takeaways.md → promote to MEMORY.md (+ memory-update.sh);
  update `specs/index.md` (014 ✅), `plan.md` status, `AGENTS.md`, `README.md`
