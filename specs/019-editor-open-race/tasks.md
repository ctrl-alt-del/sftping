# Editor First-Open Race — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted & approved
- [x] Mockup: skipped (no UI change)
- [x] test_plan.md documented

## Block 1: v1 — open() hardening
- [x] **Task 1.1**: `open()` gates on `sessionState.connected.value` (authoritative)
      and catches `IllegalStateException` → `NotConnected`; add `loaded` flag to
      `EditorUiState` — `ui/editor/EditorViewModel.kt`
- [x] **Task 1.2**: `onConnectedChanged()` reconnect self-heal (re-run cache-aware
      `open()` when stuck `NotConnected` with nothing loaded); `editable` requires
      `loaded` — `ui/editor/EditorViewModel.kt`
- [x] **Task 1.3**: v1 tests (reconnect reload, edit-preservation, dead-session open)

## Block 2: v2 — reactive Files→Editor handoff
- [x] **Task 2.1**: `SessionState.pendingEditPath` plain `@Volatile var` →
      `StateFlow<String?>` + `setPendingEdit`/`clearPendingEdit` —
      `sftp/SessionState.kt`
- [x] **Task 2.2**: `FilesViewModel.editFile()` → `setPendingEdit(file.path)`;
      `EditorViewModel` init collect opens + clears on non-null emission;
      remove `consumePendingEdit()` and the `LaunchedEffect(Unit)` consume in
      `EditorScreen` — `ui/files/FilesViewModel.kt`, `ui/editor/EditorViewModel.kt`,
      `ui/editor/EditorScreen.kt`
- [x] **Task 2.3**: tests — handoff with path set before VM creation, after VM
      creation, full FilesVM→EditorVM handoff; update StateFlow API usages —
      `EditorViewModelTest.kt`, `FilesViewModelTest.kt`

## Block 3: v3 — session health + flag independence
- [x] **Task 3.1**: keepalive 30 s → 10 s + `setServerAliveCountMax(3)`; log the
      dead-session detection — `sftp/JschSftpClient.kt`
- [x] **Task 3.2**: `open()` attempts the read unconditionally (outcome = truth;
      `IllegalStateException` → NotConnected, `SftpException` → Error);
      `editable = openLocation != null && loaded` — `ui/editor/EditorViewModel.kt`
- [x] **Task 3.3**: tests — dead-session open (read-only NotConnected), reconnect
      reload after dead-session open — `EditorViewModelTest.kt`

## Block 4: Docs
- [x] **Task 4.1**: takeaways.md → promote to MEMORY.md; update `specs/index.md`
      (019 row), `AGENTS.md`/`README.md`; plan.md status → ✅ Done
