# Retry Failed Upload — Specification

## User Story
As a user, I want to **retry a failed upload individually** from the Transfers
page, so that a one-off failure (network blip, transient server error) doesn't
force me to re-pick and re-enqueue the file.

## UX/UI
- [ ] Mockup: additive Transfers-screen affordance — no new mockup.
- A FAILED **upload** row in the Transfers list shows a trailing **Retry** icon.
- The transfer **detail dialog** for a failed upload shows a **Retry** button.
- Retrying flips the item to RUNNING, so it moves to the ACTIVE section with a
  progress bar; on success it becomes COMPLETED, on re-failure it returns to
  FAILED (retry available again).

## Acceptance Criteria

### Happy Path
- [ ] Given a failed upload, when the user taps Retry (row icon or dialog button),
  then the upload re-runs from scratch and the row moves to ACTIVE → RUNNING.
- [ ] Given the retried upload succeeds, then it shows as COMPLETED.
- [ ] Given it fails again, then it returns to FAILED with Retry available again.

### Edge Cases
- [ ] Retry is offered only for FAILED **uploads** (not downloads, not
  completed/cancelled/running items).
- [ ] If the upload's cache file was evicted, the worker reports it can't find the
  local file and WorkManager backs off (pre-existing behavior); no crash.
- [ ] Retry restarts from offset 0 (OVERWRITE), not RESUME.

## Non-Functional Requirements
- No new permissions or dependencies.
- Reuses the existing transfer pipeline (re-enqueue the same task id).
- Uploads only: a failed upload's cache (`sftping_ul_<id>_<name>`) persists (the
  worker deletes it only on success), so the worker can re-read it.
