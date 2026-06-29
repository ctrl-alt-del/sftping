# Connect Page: Password Visibility & Default Directory — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted
- [x] Mockup: none needed (additive form changes)
- [x] test_plan.md documented

## Block 1: Session plumbing
- [ ] **Task 1.1**: `homeDirectory()` on `ISftpClient`/`JschSftpClient` (via `getHome()`) + `SessionState.kt` — `sftp/`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

- [ ] **Task 1.2**: `ConnectionProfile.defaultDirectory` + JSON + test — `data/connection/`, `test/.../ConnectionProfileTest.kt`
  - Tests: `./gradlew testDebug`

## Block 2: ViewModels
- [ ] **Task 2.1**: `ConnectionViewModel` — default-dir state, resolve/persist via `SessionState`, recent prefill; update + extend VM test — `ui/connection/ConnectionViewModel.kt`, `test/.../ConnectionViewModelTest.kt`
  - Tests: `./gradlew testDebug`

- [ ] **Task 2.2**: `FilesViewModel` — seed `loadFiles` from `SessionState`; update + extend Files test — `ui/files/FilesViewModel.kt`, `test/.../FilesViewModelTest.kt`
  - Tests: `./gradlew testDebug`

## Block 3: UI
- [ ] **Task 3.1**: `ConnectionScreen` — password eye toggle + default-directory field — `ui/connection/ConnectionScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 4: Ship
- [ ] **Task 4.1**: takeaways → MEMORY, `specs/index.md` (008 ✅), README feature bullets
