# Background Transfer — Specification

Covers PRD features **F5** (background transfer + notification) and **F6**
(multi-task queue), per §5.3.

## User Stories
- As an operator, I want transfers to keep running when I switch apps or turn off
  the screen, with a notification showing progress, so long transfers complete.
- As an operator, I want a queue showing waiting tasks, current speed, and ETA.

## UX/UI
- [ ] Notification with file name, %, speed; pause/cancel actions.
- [ ] Transfers screen: active + queued tasks, per-task speed/ETA, pause/cancel.
- [ ] Mockups deferred until this feature is authored in detail.

## Acceptance Criteria

### Happy Path
- [ ] Start a transfer → background the app / lock screen → transfer continues →
      notification updates progress + speed → completes.
- [ ] Multiple queued transfers run/await per policy; queue UI reflects state.
- [ ] Pause from the notification or UI → resumes correctly (offset preserved).

### Edge Cases
- [ ] Notification permission denied (API 33+) → transfer still runs; user warned.
- [ ] OS reclaims the worker (time limits) → task re-enqueues and resumes.
- [ ] Connectivity lost → worker waits on `NetworkType.CONNECTED` constraint.

## Non-Functional Requirements
- Foreground service type `dataSync`; respect Android 15+ daily FGS limits.
- Notification updates throttled to avoid spam (e.g. ≤ 1/sec).
- **Out of scope**: extreme-failure hardening (005).
