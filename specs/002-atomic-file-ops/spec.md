# Atomic File Operations — Specification

Covers PRD feature **F3**: download, upload, delete, rename — single-shot, no
resume, no background (those are 003 / 004).

## User Stories
- As an operator, I want to **upload** a local phone file to the current remote
  directory so I can deploy artifacts.
- As an operator, I want to **download** a remote file to local storage.
- As an operator, I want to **delete** and **rename** remote files/folders.

## UX/UI
- [x] **Upload** flow mocked: `ux-ui/mockups.png` — FAB speed-dial → system SAF
      picker → confirm sheet (destination + overwrite check) → progress in Transfers.
- [ ] **Download**, **delete**, **rename** mockups — deferred; to be produced when
      this feature is authored in detail.

## Acceptance Criteria

### Happy Path
- [ ] Upload: pick a local file via SAF → confirm destination = current remote
      dir → file transfers → appears in the remote listing.
- [ ] Download: choose a remote file → pick a SAF destination → file is written
      locally (via private-cache-then-copy) and verified by size.
- [ ] Delete: confirm dialog → remote entry removed → list refreshes.
- [ ] Rename: inline dialog → remote entry renamed → list refreshes.

### Edge Cases
- [ ] Upload where the name already exists → overwrite confirmation.
- [ ] Cancel mid-transfer → stream aborted, partial file cleaned up.
- [ ] Delete non-empty folder → recursive-delete confirmation.
- [ ] Insufficient remote permissions → clear error, no partial state.
- [ ] SAF permission revoked / Uri stale → prompt to re-pick destination.

## Non-Functional Requirements
- Streamed I/O with a bounded buffer (no `readAllBytes`); foreground only.
- Cancellation is prompt and leaves no orphaned partial files.
- **Out of scope**: resume (003), background/notifications/queue (004).
