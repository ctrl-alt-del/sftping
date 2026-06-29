# Files Page: Hidden Toggle, Sort & Search — Specification

## User Story
As a user browsing a remote directory, I want to (1) toggle visibility of hidden
(dot) files, (2) choose how the listing is sorted, and (3) filter the current
folder by name, so that I can find and organise files quickly.

## UX/UI
- [ ] Mockup: additive changes to the existing Files screen — no new mockup.
- **Search**: a Search action in the app bar reveals a text row (leading search
  icon, trailing clear) that filters the current directory's entries (folders and
  files) by name as you type. Filters the current folder only (not recursive).
- **Sort / hidden menu**: a sort/filter action opens a dropdown with four sort
  options (radio-style, active one marked) — Name (A–Z), Name (Z–A), Size,
  Last modified — plus a checkable "Show hidden files" item.
- Existing Refresh action is retained.

## Acceptance Criteria

### Happy Path
- [ ] Given hidden files exist, when "Show hidden files" is off (default), then
  entries whose name starts with `.` are not listed; toggling on reveals them.
- [ ] Given a directory listing, when the user picks a sort option, then the list
  reorders accordingly with **folders always first**, then files: Name A–Z / Z–A,
  Size (largest first), Last modified (newest first). Default is Name (A–Z).
- [ ] Given the user types `log` in search, then only entries whose name contains
  `log` (case-insensitive) remain; clearing the query restores the full listing.
- [ ] Sorting/filtering happen in-memory with no additional SFTP round-trips.

### Edge Cases
- [ ] When a search/filter yields nothing, then an informative empty state is shown
  ("No matching files") distinct from an empty folder ("This folder is empty").
- [ ] Sort mode and the hidden toggle persist across folder navigation; the search
  query clears when navigating into/out of a folder.
- [ ] Within the folders group and the files group, ties break by name for a stable
  order (e.g. equal sizes, or directories under Size sort).

## Non-Functional Requirements
- Performance: filtering/sorting are pure, in-memory operations over the already
  fetched listing; no re-listing on toggle/keystroke.
- Accessibility: actions have content descriptions.
- Offline: operates on the in-memory listing; no network beyond the initial list.
