# Grouped Transfers — Tasks

## Block 0: Spec & Design (before code)
- [x] `doc-coauthoring`: spec.md + plan.md approved
- [x] `canvas-design`: mockup generated in `ux-ui/`
- [x] `test_plan.md`: test scenarios documented

## Block 1: Domain Layer

- [x] **Task 1.1**: Add `retryAllFailed()` to `TransferManager` + unit test — `transfer/TransferManager.kt`, `app/src/test/java/com/example/sftping/transfer/TransferManagerTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: UI Layer

- [x] **Task 2.1**: Add `retryAllFailed()` to `TransfersViewModel` + restructure `TransfersScreen` with collapsible groups, Retry all button, and multi-select scoped to Completed — `ui/transfers/TransfersViewModel.kt`, `ui/transfers/TransfersScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`
