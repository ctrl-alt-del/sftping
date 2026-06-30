# Grouped Transfers — Test Plan

## Unit Tests (JVM: `./gradlew testDebug`)

### TransferManager.retryAllFailed()

- [ ] **Happy path — retries all FAILED uploads**: Arrange — insert 2 FAILED+UPLOAD tasks, 1 FAILED+DOWNLOAD task, 1 COMPLETED+UPLOAD. Mock `RetryUseCase` via mockito. Act — call `retryAllFailed()`. Assert — `RetryUseCase.execute()` called exactly twice (only FAILED+UPLOAD), never called for download or completed.

- [ ] **Empty list — no failures present**: Arrange — insert 0 tasks or only COMPLETED tasks. Act — call `retryAllFailed()`. Assert — `RetryUseCase.execute()` never called, no crash, returns normally.

- [ ] **All failures are downloads (non-retryable)**: Arrange — insert 3 FAILED+DOWNLOAD tasks. Act — call `retryAllFailed()`. Assert — `RetryUseCase.execute()` never called (individual `RetryUseCase` guard would also skip, but we filter before calling).

- [ ] **Mixed statuses — only FAILED+UPLOAD retried**: Arrange — insert FAILED+UPLOAD, FAILED+DOWNLOAD, RUNNING+UPLOAD, PAUSED+DOWNLOAD, COMPLETED+UPLOAD, CANCELLED+UPLOAD. Act — call `retryAllFailed()`. Assert — exactly 1 call to `RetryUseCase.execute()` for the FAILED+UPLOAD item.

### Edge Cases

- [ ] **Dao returns null for a task mid-loop**: Not applicable — `dao.all()` returns a snapshot list and `RetryUseCase` handles null returns internally.

## UI / Integration (manual / instrumented: `./gradlew connectedDebugAndroidTest`)

- [ ] **Three sections render**: Connect to SFTP, trigger an upload that fails, complete a download. Open Transfers tab — see Active (if running) + Failed + Completed sections, each with header, count, and separator.

- [ ] **Failed section collapse/expand**: Tap the Failed chevron — list animates out, chevron rotates. Tap again — list animates in.

- [ ] **Completed section collapse/expand**: Tap the Completed chevron — list animates out, chevron rotates. Tap again — list animates in.

- [ ] **Retry all button**: Tap "Retry all" in Failed section — all FAILED+UPLOAD items move to Active, downloads stay. Verify via visual inspection.

- [ ] **Empty groups hidden**: Cancel all active transfers, delete all completed. Only non-empty groups render.

## Edge Cases

- [ ] Active section never has a chevron or collapse affordance.
- [ ] Multi-select only affects COMPLETED group; FAILED items cannot be selected.
- [ ] Zero transfer list — only empty state is shown (no headers).
