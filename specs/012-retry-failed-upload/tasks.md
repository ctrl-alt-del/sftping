# Retry Failed Upload — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted
- [x] Mockup: none (additive affordance)
- [x] test_plan.md documented

## Block 1: Retry use case
- [ ] **Task 1.1**: `RetryUseCase` (reset offset + re-enqueue, gated FAILED+UPLOAD) + tests — `transfer/usecase/RetryUseCase.kt`, `test/.../ManagementUseCaseTest.kt`
  - Tests: `./gradlew testDebug`

## Block 2: Manager wiring
- [ ] **Task 2.1**: `TransferManager.retry()` + ctor param; fix `TransferManagerTest` constructions — `transfer/TransferManager.kt`, `test/.../TransferManagerTest.kt`
  - Tests: `./gradlew testDebug`

## Block 3: UI
- [ ] **Task 3.1**: `TransfersViewModel.retry()` + `TransfersScreen` row Retry icon + dialog Retry button — `ui/transfers/TransfersViewModel.kt`, `ui/transfers/TransfersScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 4: Ship
- [ ] **Task 4.1**: takeaways → MEMORY, `specs/index.md` (012 ✅), README Transfers bullet
