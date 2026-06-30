---
feature_id: "013"
name: "Grouped Transfers — Collapsible Sections & Batch Retry"
status: "✅ Done"
depends_on: ["012"]
touches:
  - "transfer/TransferManager.kt"
  - "ui/transfers/TransfersScreen.kt"
  - "ui/transfers/TransfersViewModel.kt"
created: "2026-06-30"
---

# Grouped Transfers — Plan

## Approach

Restructure the Transfers screen from two static sections (ACTIVE / DONE) to three sections (ACTIVE / FAILED / COMPLETED), with the latter two collapsible via `AnimatedVisibility` + rotating chevron. Add a `retryAllFailed()` method that queries FAILED+UPLOAD tasks from the DAO and loops through existing `RetryUseCase` logic. Keep Active always visible (no collapse). Scope multi-select to Completed group only.

Grouping logic is pure Compose — computed from the existing `manager.items` StateFlow, no new DAO queries. "Retry all" drives through `TransferManager` → `RetryUseCase` (reuse existing single-item retry).

## Files to Change

| Action | File | Rationale |
|--------|------|-----------|
| Modify | `transfer/TransferManager.kt` | Add `retryAllFailed()` suspend function |
| Modify | `ui/transfers/TransfersViewModel.kt` | Add `retryAllFailed()` wrapper + expose via `viewModelScope.launch` |
| Modify | `ui/transfers/TransfersScreen.kt` | Three-group layout, collapsible sections, "Retry all" button, multi-select scoped to Completed only |
| Modify | `app/src/test/.../transfer/TransferManagerTest.kt` | Test `retryAllFailed()` |

No new files. No DI changes. No dependency bumps.

## Risks

- **RetryAll deleting non-UPLOAD items**: Mitigated by existing `RetryUseCase` guard (returns early for non-FAILED, non-UPLOAD). `retryAllFailed()` filters before calling.
- **Collapse state loss on tab switch**: `rememberSaveable` may be needed if collapse state must survive process death. Acceptable to reset collapse on tab re-entry; Observers of `SessionState.epoch` pattern (010) do not apply here — transfers are global, not session-scoped.
- **Animation jank with many items**: Mitigated by `LazyColumn` + `AnimatedVisibility` (only renders visible items). Section count is capped by DB size (typically < 100).

## Dependencies

- 012 (Retry Failed Upload): `RetryUseCase` is the foundation for `retryAllFailed()`.
