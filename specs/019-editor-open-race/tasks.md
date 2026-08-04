# Editor First-Open Race — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted & approved
- [x] Mockup: skipped (no UI change)
- [x] test_plan.md documented

## Block 1: Fix
- [x] **Task 1.1**: `open()` gates on `sessionState.connected.value` (authoritative)
      and catches `IllegalStateException` → `NotConnected`; add `loaded` flag to
      `EditorUiState` (set in cache/read success branches, reset in NotConnected/
      Error/close) — `ui/editor/EditorViewModel.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 1.2**: `onConnectedChanged()` reconnect recovery — on `false → true`,
      after the pending-edit flush, re-run cache-aware `open()` when
      `!loaded && saveStatus is NotConnected`; `editable` now also requires `loaded`
      — `ui/editor/EditorViewModel.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 1.3**: Tests — reconnect reloads a file that opened while disconnected;
      reconnect keeps in-memory edits of an already loaded file; open with dead
      session maps to NotConnected (no crash) — `app/src/test/.../EditorViewModelTest.kt`
  - Tests: `./gradlew testDebug`

## Block 2: Docs
- [x] **Task 2.1**: takeaways.md → promote to MEMORY.md; update `specs/index.md`
      (019 row), `AGENTS.md`/`README.md` known-gaps; flip plan.md status → ✅ Done
