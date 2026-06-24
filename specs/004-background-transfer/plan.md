---
feature_id: "004"
name: "Background Transfer"
status: "📋 Planned"
depends_on: ["003"]
touches:
  - "app/src/main/AndroidManifest.xml"
  - "app/src/main/java/com/example/sftping/work/"
  - "app/src/main/java/com/example/sftping/ui/transfers/"
  - "gradle/libs.versions.toml"
  - "app/build.gradle.kts"
created: "2026-06-25"
---

# Background Transfer — Plan

> Seed spec from PRD F5/F6 + §5.3. Tasks detailed when started.

## Approach
Run transfers reliably when the app is backgrounded or the screen is off, with a
notification showing progress, plus a multi-task queue.

- **WorkManager + `@HiltWorker`** (`CoroutineWorker`) injected via `HiltWorkerFactory`.
- **Foreground service**: declare `foregroundServiceType="dataSync"` +
  `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC` / `POST_NOTIFICATIONS`.
  `setForeground(...)` shows file name, percentage, speed.
- **Constraints**: `NetworkType.CONNECTED`.
- **Pause/resume** (per SDD): pause stops the worker + persists offset (003);
  resume enqueues a new worker continuing from `transferredBytes`. No busy-wait.
- **Queue (F6)**: waiting list, active task, instantaneous speed, ETA.

> See MEMORY `#build`: Worker execution-window limits and the `dataSync` FGS
> ~6h/day cap (Android 15+) drive the re-enqueue pause model.

## Files to Create / Change
| Action | File | Rationale |
|--------|------|-----------|
| Change | `gradle/libs.versions.toml`, `app/build.gradle.kts` | Add WorkManager + Hilt-Work |
| Change | `AndroidManifest.xml` | FGS type + permissions |
| Create | `work/*` | `SftpTransferWorker` (HiltWorker), notification builder |
| Change | `ui/transfers/*` | Queue UI: speed, ETA, pause/cancel |

## Risks
- High-API FGS restrictions and notification permission (API 33+) UX.
- WorkManager + Hilt factory wiring; expedited/foreground promotion timing.

## Dependencies
Requires 003. Unblocks 005.
