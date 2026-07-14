# Files Item Actions (Long-Press Context Menu) — Specification

## User Story
As a user browsing remote files, I want to long-press an item to get quick
actions — copy its full remote path, open it in the editor, or start a
multi-selection — so that common per-item operations are one gesture away.

## UX/UI
- [ ] No new mockup — additive change to the existing Files screen (follows 009/011
      precedent). Long-press opens a Material 3 `DropdownMenu` anchored to the row,
      reusing the pattern already used in `EditorScreen` (`LocationRow`).
- Menu items (top to bottom):
  - **Copy path** — always shown (files and folders).
  - **Edit** — shown only when the item is a non-directory file whose name matches
    the editable text-type allowlist.
  - **Select** — always shown; enters multi-select mode.

## Acceptance Criteria

### Happy Path
- [ ] Given the Files list is shown and I am not in multi-select mode, when I
      long-press a row, then a context menu appears with Copy path / (Edit) / Select.
- [ ] Given the context menu is open, when I tap **Copy path**, then the item's full
      absolute remote path is placed on the system clipboard and a snackbar
      "Path copied" is shown.
- [ ] Given an editable text file (e.g. `.txt`, `.md`, `.json`, `.conf`), when I tap
      **Edit**, then the app switches to the Editor tab and opens that file
      transiently (not persisted to the saved locations list).
- [ ] Given the context menu is open, when I tap **Select**, then multi-select mode is
      entered with that item selected; subsequent taps toggle selection as today.

### Edge Cases
- [ ] Given I am already in multi-select mode, when I long-press or tap a row, then it
      toggles selection (no context menu appears).
- [ ] Given a directory or a non-editable file (e.g. `.png`, `.zip`, no extension),
      when I open the context menu, then the **Edit** item is not shown.
- [ ] Given I tap **Edit** and open a file I cannot write (e.g. root-owned), when I
      later save, then the editor shows a clear "Permission denied" error and does
      **not** silently cache the edit as pending-sync (that fallback is reserved for
      connectivity loss).
- [ ] Given the item path is at root (e.g. `/etc/hosts`), when I Copy path, then the
      full path including the leading slash is copied.

## Non-Functional Requirements
- Performance: menu, copy, and type-check are pure in-memory operations; no extra
  network round-trips. Edit reuses the existing single `readText` on open.
- Accessibility: menu items carry content descriptions / leading icons consistent
  with the Editor menu.
- Offline: Copy path and the type-check work offline. Edit opens read-only when
  disconnected (existing Editor behavior).

## Out of Scope (follow-ups)
- Capturing POSIX mode/owner into `RemoteFile` and showing a read-only banner or a
  permission lock badge (Option B) — deferred.
- Any permission pre-check to hide/disable **Edit** — intentionally not done; SFTP
  cannot reliably predict write access, so we surface failures at save time.
</content>
</invoke>
