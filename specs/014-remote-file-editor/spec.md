# Remote File Editor — Specification

## User Story
As a user, I want a dedicated **Editor** tab where I can save a list of remote
file locations and edit a text file in place, so that I can make quick edits to
config/scripts without a full download → edit → re-upload cycle. Edits should
**autosave**, **survive being offline** (cached locally and re-synced on
reconnect), and support **undo/redo** and a manual **save**.

## UX/UI
- [x] Mockup: `ux-ui/mockups.png` (4 frames: locations list → connected edit →
  offline cached → disconnected/greyed).
- New **Editor** tab is added as the 4th `NavigationSuiteScaffold` destination:
  **Connect → Files → Transfers → Editor** (icon: `Icons.Filled.Edit`). It is
  always accessible, independent of connection state.
- **Locations list** (no file open): a list of saved locations, each showing a
  label, the mono-spaced remote path, and a **sync dot** (amber = pending offline
  edits, otherwise none). A **+ Add location** button opens a sheet with `Label`
  and `Remote path` fields. Each row has an overflow menu with **Edit** and
  **Delete**. Managing locations works **whether or not** the server is connected.
- **Editor pane** (a location is open): a top toolbar with **back**, the file
  name/path chip, and three trailing icon buttons — **Undo**, **Redo**, **Save**.
  Below is a monospace multi-line text field, and a bottom **status bar**.
- **Connection-gated editing**: when **disconnected**, the text field is greyed
  out and read-only, the toolbar Undo/Redo/Save icons are disabled, and the
  status bar reads "Not connected — connect to edit." When **connected**, editing
  is enabled.
- **Status bar states**: `Saved to server · <time>` (green), `Saving…`,
  `Cached · pending sync (offline)` (amber), `Not connected — connect to edit`
  (grey), or an error message.
- **Offline banner**: while editing a file that has un-synced cached changes, a
  banner explains edits are cached and will sync on reconnect.

## Acceptance Criteria

### Happy Path
- [ ] The Editor tab appears as the 4th tab and is reachable regardless of
  connection state.
- [ ] Adding a location (label + remote path) persists it; it appears in the list
  and **survives an app restart**.
- [ ] Editing a saved location's label/path updates it; deleting removes it.
- [ ] Opening a location while **connected** loads the remote file's text into the
  editor (UTF-8).
- [ ] Typing then pausing (~2s debounce) autosaves to the remote file; the status
  bar shows `Saving…` then `Saved to server · <time>`.
- [ ] Tapping the **Save** icon flushes immediately (no wait for debounce).
- [ ] **Undo** reverts to the previous text snapshot; **Redo** re-applies it. The
  icons are enabled only when there is something to undo/redo.
- [ ] When a save fails or the app is offline, the current text is written to a
  **local cache** and the location is flagged **pending sync**; the status bar
  shows `Cached · pending sync (offline)`.
- [ ] After reconnecting (Connect tab), the open file's cached (pending) edits are
  **flushed to the remote** automatically; on success the pending flag clears and
  the status bar returns to `Saved to server`.
- [ ] Reopening a location that has pending cached edits loads the **cached**
  content (not the stale remote), so unsynced work is never lost.

### Edge Cases
- [ ] Opening a location while **disconnected** shows the last cached content if
  present (read-only, greyed) or an empty greyed pane, plus "Not connected."
- [ ] Opening a path that does not exist on the server surfaces a clear error;
  the editor stays read-only (no create-on-save in this feature).
- [ ] A blank label defaults to the file name (last path segment); a blank remote
  path is rejected (Add/Save disabled).
- [ ] Undo at the beginning / redo at the end of history are no-ops (icons
  disabled).
- [ ] Making a new edit after undoing truncates the redo branch.
- [ ] Deleting a location also drops any pending cached edit for that path.
- [ ] Losing the connection mid-edit (session drops) transitions the pane to
  disconnected/greyed without crashing; typed-but-unsaved text is cached.

## Non-Functional Requirements
- **Performance**: text I/O and SFTP calls run off the main thread
  (`Dispatchers.IO`); autosave is debounced to avoid a write per keystroke.
- **Offline**: pending edits are persisted in a dedicated Room DB
  (`sftping_editor.db`) so they survive process death; re-sync is triggered on
  reconnect.
- **Scope**: text-based files only; one file open at a time; edit existing files
  only (no create-on-save); last-write-wins on save (no server-side conflict
  detection — documented limitation).
- **Security**: no new credentials/permissions; reuses the existing authenticated
  SFTP session. Cached file contents live in app-private storage only.
- **Concurrency**: text read/write open a **fresh `ChannelSftp` per operation**
  over the shared session (consistent with the per-operation channel fix).
