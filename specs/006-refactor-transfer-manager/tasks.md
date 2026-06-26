# TransferManager Refactor — Tasks

> Feature 006: Decouple TransferManager using Strategy + UseCase patterns.
> One task = one commit. Build (`./gradlew assembleDebug`) + tests
> (`./gradlew testDebug`) must pass per task.

## Block 0: Spec & design
- [x] Spec + plan authored
- [ ] Test plan approved
- [ ] Tasks approved

## Block 1: Interface & protocol layer
- [x] **T6.1**: Introduce `TransferStrategy` interface and `TransferProgress` data class
- [x] **T6.2**: Implement `SftpTransferStrategy` — bridge JSch callbacks to `Flow<TransferProgress>` via `callbackFlow`

## Block 2: Business logic
- [x] **T6.3**: Extract `DownloadUseCase` — offset, cache path, progress persistence
- [x] **T6.4**: Extract `UploadUseCase` — resume offset, local file check
- [x] **T6.5**: Extract `EnqueueUseCase`, `PauseUseCase`, `ResumeUseCase`, `CancelUseCase`

## Block 3: Coordinator & worker
- [x] **T6.6**: Convert `TransferManager` to thin coordinator — delegates all operations to UseCases
- [x] **T6.7**: Delegate `SftpTransferWorker` to `DownloadUseCase` / `UploadUseCase`

## Block 4: Tests
- [ ] **T6.8**: Unit tests for `SftpTransferStrategy` (mock ISftpClient)
- [ ] **T6.9**: Unit tests for `DownloadUseCase` / `UploadUseCase` (mock strategy + fake DAO)
- [ ] **T6.10**: Update `TransferManagerTest` for UseCase delegation
