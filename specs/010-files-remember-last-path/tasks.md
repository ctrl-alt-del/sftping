# Files Page: Remember Last Visited Path — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted
- [x] Mockup: none (behavior only)
- [x] test_plan.md documented

## Block 1: Session epoch
- [ ] **Task 1.1**: `SessionState.epoch` + bump in `ConnectionViewModel.onConnected`; assert epoch bump in VM test — `sftp/SessionState.kt`, `ui/connection/ConnectionViewModel.kt`, `test/.../ConnectionViewModelTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: Files remember path
- [ ] **Task 2.1**: `FilesViewModel.onEnterScreen()` + `loadedEpoch`; wire `FilesScreen` `LaunchedEffect`; add Files VM tests — `ui/files/FilesViewModel.kt`, `ui/files/FilesScreen.kt`, `test/.../FilesViewModelTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
  - Lint: `./gradlew lint`

## Block 3: Ship
- [ ] **Task 3.1**: takeaways → MEMORY, `specs/index.md` (010 ✅), README file-browser bullet
