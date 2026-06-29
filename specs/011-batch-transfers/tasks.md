# Batch Transfers & Uploaded-File Memory — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted
- [x] Mockup: none (additive surfaces)
- [x] test_plan.md documented

## Block 1: Uploaded memory
- [ ] **Task 1.1**: `TransferManager.completedUploadPaths()` + test — `transfer/TransferManager.kt`, `test/.../TransferManagerTest.kt`
  - Tests: `./gradlew testDebug`

## Block 2: Upload selection model + VM
- [ ] **Task 2.1**: `UploadCandidate` + pure `buildUploadCandidates()` + test — `ui/files/UploadCandidate.kt`, `test/.../UploadCandidateTest.kt`
  - Tests: `./gradlew testDebug`
- [ ] **Task 2.2**: `FilesViewModel` prepareUpload/toggle/confirm/cancel + enqueueUpload refactor + batch observer + tests — `ui/files/FilesViewModel.kt`, `test/.../FilesViewModelTest.kt`
  - Tests: `./gradlew testDebug`

## Block 3: Upload UI
- [ ] **Task 3.1**: `FilesScreen` multi-pick launcher + Upload selection sheet — `ui/files/FilesScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 4: Multi-download
- [ ] **Task 4.1**: `androidx.documentfile` dep; `FilesViewModel.downloadFiles`/`enqueueDownload`; `FilesScreen` tree picker + multi-download enablement — catalog, `app/build.gradle.kts`, `ui/files/FilesViewModel.kt`, `ui/files/FilesScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 5: Ship
- [ ] **Task 5.1**: takeaways → MEMORY, `specs/index.md` (011 ✅), README features
