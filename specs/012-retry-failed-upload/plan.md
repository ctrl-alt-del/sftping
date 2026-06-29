---
feature_id: "012"
name: "Retry Failed Upload"
status: "✅ Done"
depends_on: ["002"]
touches:
  - "app/src/main/java/com/example/sftping/transfer/usecase/RetryUseCase.kt"
  - "app/src/main/java/com/example/sftping/transfer/TransferManager.kt"
  - "app/src/main/java/com/example/sftping/ui/transfers/TransfersViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/transfers/TransfersScreen.kt"
  - "app/src/test/java/com/example/sftping/transfer/usecase/ManagementUseCaseTest.kt"
  - "app/src/test/java/com/example/sftping/transfer/TransferManagerTest.kt"
created: "2026-06-30"
---

# Retry Failed Upload — Plan

## Approach

A failed upload lives in the Transfers "done" list. Retry reuses the resume
machinery: a new `RetryUseCase` resets the task's offset to 0
(`dao.updateProgress(id, 0, RUNNING)`) and re-enqueues a `OneTimeWorkRequest`
(tag `sftping_transfer_<id>`, `task_id`). The worker re-reads the still-present
upload cache file (`sftping_ul_<id>_<name>` — deleted only on success) and
re-uploads with OVERWRITE (offset 0 → not RESUME, avoiding the RESUME-on-fresh
failure). Status → RUNNING moves the row back to ACTIVE.

Gated to FAILED + UPLOAD: downloads need a UI-driven cache→SAF copy step that the
Transfers page can't replicate, so download retry is out of scope.

`TransferManager.retry(id)` delegates to `RetryUseCase` (Hilt `@Inject` ctor — no
module). `TransfersViewModel.retry(id)` wraps it. The UI adds a trailing Retry
icon on failed-upload `DoneCard`s and a Retry button in the detail dialog.

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `transfer/usecase/RetryUseCase.kt` | reset offset + re-enqueue, gated to FAILED+UPLOAD |
| Edit | `transfer/TransferManager.kt` | `retry()` + ctor param |
| Edit | `ui/transfers/TransfersViewModel.kt` | `retry(id)` |
| Edit | `ui/transfers/TransfersScreen.kt` | row Retry icon + dialog Retry button |
| Edit | `test/.../ManagementUseCaseTest.kt` | RetryUseCase tests |
| Edit | `test/.../TransferManagerTest.kt` | add `mockRetry` to constructions |

## Risks
- Adding a ctor param to `TransferManager` breaks existing test constructions —
  update them (3 sites).
- Cache eviction edge: handled gracefully by the existing worker path.

## Dependencies
- 002 (transfers pipeline + use cases).

## ADR-011
Retry re-enqueues the existing task (resume mechanics) after resetting
`transferredBytes` to 0 (fresh OVERWRITE). Scoped to uploads — downloads need a
live SAF-copy step the Transfers page can't replicate.
