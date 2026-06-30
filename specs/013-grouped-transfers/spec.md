# Grouped Transfers with Collapsible Sections & Batch Retry — Specification

## User Story
As a user managing many transfers, I want failed transfers grouped separately from completed ones, both lists collapsible, and a one-tap "Retry all" for failures so that I can triage results quickly without scrolling through noise.

## UX/UI
- [x] Mockup: `ux-ui/transfers-mockup.png`

Three vertical sections in the transfers screen:

### 1. Active (always visible, no collapse)
- Contains RUNNING + PAUSED transfers.
- Each card shows direction icon, file name, progress bar, status badge.
- Swipe EndToStart → cancel (existing behavior, unchanged).
- Hidden entirely when empty.

### 2. Failed (collapsible, default expanded)
- Contains FAILED transfers.
- Section header shows a colored dot, "FAILED (N)" label, chevron icon, and a **"Retry all"** `FilledTonalButton`.
- Expanding/collapsing uses `AnimatedVisibility` with a rotating chevron.
- Per-item retry icon (Refresh) shown on UPLOAD rows only (existing guard: downloads cannot be retried).
- Swipe EndToStart → dismiss (cancels + deletes, same as today).
- Hidden entirely when empty.
- "Retry all" button:
  - Calls `viewModel.retryAllFailed()`.
  - Silently skips non-retryable items (downloads, non-FAILED).
  - Retried items move to Active group (status resets to RUNNING).

### 3. Completed (collapsible, default expanded)
- Contains COMPLETED transfers.
- Section header shows a colored dot, "COMPLETED (N)" label, chevron icon, and a **"Select"** `TextButton`.
- Expanding/collapsing uses `AnimatedVisibility` with a rotating chevron.
- Multi-select entry: "Select" button selects all COMPLETED items (bulk-select within this group only — FAILED items are excluded).
- In select mode: contextual top bar with "N selected" + Clear + Delete-all.
- Tap opens detail `AlertDialog` (unchanged).
- Swipe EndToStart → dismiss (unchanged).
- Hidden entirely when empty.

## Acceptance Criteria

### Happy Path
- [ ] Given transfers in all three statuses, when I open the Transfers tab, then I see Active, Failed, and Completed sections — each clearly separated with headers, counts, and styling.
- [ ] Given a Failed section with mixed uploads and downloads, when I tap "Retry all", then all FAILED+UPLOAD items reset to RUNNING and appear in Active; downloads remain FAILED.
- [ ] Given a Failed section, when I tap the chevron, then the list collapses/expands with animation and the chevron rotates.
- [ ] Given a Completed section, when I tap the chevron, then the list collapses/expands with animation and the chevron rotates.
- [ ] Given a Completed section, when I tap "Select", then all COMPLETED items become checked and the contextual bar appears.

### Edge Cases
- [ ] When no transfers exist in a group, the entire section (header + items) is hidden.
- [ ] When "Retry all" is tapped but all FAILED items are downloads (non-retryable), nothing happens — no crash, no empty enqueue.
- [ ] When a retry fails again, the item returns to the Failed section.
- [ ] Active section is never collapsible — no chevron, no collapse affordance.
- [ ] Multi-select only affects COMPLETED items; FAILED items are not selectable.

## Non-Functional Requirements
- Performance: Collapse/expand animations run at 60 fps. No new Room queries — grouping is computed in the Compose layer from the existing `manager.items` StateFlow.
- Accessibility: Section headers have content descriptions including count; chevron buttons announce expand/collapse state.
- No new dependencies, no DI changes, no new files outside `ui/transfers/` and `transfer/TransferManager.kt`.
