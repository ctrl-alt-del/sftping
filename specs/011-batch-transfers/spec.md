# Batch Transfers & Uploaded-File Memory — Specification

## User Story
As a user, I want to (1) select **multiple files to upload or download at once**,
and (2) have the app **remember which files I've already uploaded** and **show an
indication** in the upload selection list, so that bulk transfers are fast and I
don't accidentally re-upload the same files.

## UX/UI
- [ ] Mockup: additive Files-screen surfaces — no new mockup.
- **Multi-upload**: the upload action opens the system multi-file picker; after
  picking, an in-app **Upload selection** sheet lists the chosen files with a
  checkbox each, file name + size, and an **"Uploaded" badge** on files that were
  previously uploaded into the current folder. Confirm uploads all checked files.
- **Multi-download**: multi-select in the file list allows downloading more than
  one file; a destination **folder** is picked (system tree picker) and all
  selected files are saved into it. A single selected file keeps the existing
  "save as" flow.

## Acceptance Criteria

### Happy Path
- [ ] Picking N files for upload enqueues N upload tasks (for the checked ones).
- [ ] A file previously uploaded into the current directory shows an "Uploaded"
  badge in the selection sheet, but remains checked/selectable.
- [ ] Selecting N remote files and choosing a destination folder downloads all of
  them into that folder.
- [ ] The "uploaded" memory persists across app restarts (backed by the existing
  `transfer_tasks` history).

### Edge Cases
- [ ] Directories selected for download are excluded (download applies to files).
- [ ] Picking zero files (cancel) does nothing.
- [ ] Unchecking all files in the selection sheet uploads nothing.
- [ ] The "Uploaded" badge is keyed by target remote path (`currentDir/name`), so
  it reflects "already uploaded into this folder" and persists even if the remote
  file was later deleted.

## Non-Functional Requirements
- No new runtime permissions (SAF only).
- Uploaded-memory is derived from completed UPLOAD transfer records (no new schema).
- Concurrency: batch transfers run as independent tasks; safe after the
  per-operation SFTP channel fix.
