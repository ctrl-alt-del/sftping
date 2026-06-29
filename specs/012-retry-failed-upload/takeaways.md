# Retry Failed Upload — Takeaways

## What Went Well
- `RetryUseCase` mirrors `ResumeUseCase` structurally, making it a trivial add — the
  re-enqueue + tag pattern was already proven.
- Gating to FAILED + UPLOAD at both the UI and use‑case level prevents silent failures
  (downloads would appear to retry but never deliver to SAF).
- The existing cache persistence on upload failure (worker deletes only on success) is
  the enabling property — no new state management needed.

## What We Learned
- A "retry" on an upload is mechanically a "resume from offset 0" — the same
  `OneTimeWorkRequest` pattern works; just reset `transferredBytes` so the strategy
  routes to OVERWRITE rather than RESUME.
- `TransferManager` constructor churn bleeds into `TransferManagerTest` (3 construction
  sites) — worth noting for future additions.

## API / Tech Surprises
- `WorkManager.getInstance(context)` throws `IllegalStateException` in JVM tests; all
  management use cases already swallow it, so unit tests can verify DAO interactions
  without real WorkManager.
- `AlertDialog`'s `dismissButton` is nullable — clean way to show a "Retry" button only
  for failed uploads without a second dialog variant.

## Patterns Worth Reusing
<!-- These get promoted to root MEMORY.md after ship -->
- Per-item retry via re-enqueue + offset reset → OVERWRITE (see MEMORY
  "Patterns That Worked" and ADR‑011).
