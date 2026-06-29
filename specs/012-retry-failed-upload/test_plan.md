# Retry Failed Upload — Test Plan

## Unit Tests

### RetryUseCase (JVM)
- [ ] **retries a failed upload**: Arrange `dao.get(id)` → FAILED UPLOAD task; Act
  `execute(id)`; Assert `dao.updateProgress(id, 0, RUNNING)` called (offset reset +
  status RUNNING). (WorkManager.getInstance throws in JVM and is swallowed.)
- [ ] **no-op for non-failed**: Arrange a COMPLETED task; Act `execute`; Assert
  `updateProgress` not called.
- [ ] **no-op for failed download**: Arrange a FAILED DOWNLOAD task; Act `execute`;
  Assert `updateProgress` not called.
- [ ] **no-op when task missing**: `dao.get` → null; Assert `updateProgress` not called.

### TransferManager (JVM)
- [ ] **existing tests pass** with the new `RetryUseCase` constructor arg.

## Integration / UI (manual)
- [ ] Fail an upload → row shows Retry icon → tap → moves to ACTIVE → RUNNING →
  COMPLETED on success.
- [ ] Detail dialog of a failed upload shows a Retry button.
- [ ] Failed downloads / completed / cancelled rows show no Retry affordance.

## Edge Cases
- [ ] Cache evicted → graceful failure (no crash).
- [ ] Retry uses OVERWRITE (offset 0), not RESUME.
