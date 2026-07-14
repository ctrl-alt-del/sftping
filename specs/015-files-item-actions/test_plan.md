# Files Item Actions (Long-Press Context Menu) — Test Plan

## Unit Tests

### EditableFileType
- [ ] **Happy path**: `isEditable("app.conf")`, `isEditable("notes.md")`,
      `isEditable("data.json")` → true (allowlist hit, case-insensitive).
- [ ] **Non-editable**: `isEditable("photo.PNG")`, `isEditable("archive.zip")` → false.
- [ ] **No extension**: `isEditable("README")`, `isEditable("Makefile")` → policy
      decision documented (dotless known names / plain names) → false unless allowlisted.
- [ ] **Dotfiles**: `isEditable(".bashrc")` → true (known shell dotfile) or via base rule.
- [ ] **Boundary**: empty string / trailing dot → false, no crash.

### FilesViewModel.copyPath
- [ ] **Happy path**: Arrange VM with `InMemoryClipboard`; Act `copyPath(file)`;
      Assert clipboard holds `file.path` and a "Path copied" message was emitted.
- [ ] **Root path**: copying `/etc/hosts` copies the full leading-slash path.

### FilesViewModel.editFile
- [ ] **Happy path**: Act `editFile(file)`; Assert `sessionState.pendingEditPath ==
      file.path` and a navigate-to-editor event was emitted.

### EditorViewModel (pendingEditPath + save error)
- [ ] **Consume pending path**: given `sessionState.pendingEditPath` set and connected,
      the VM opens that path transiently (content loaded via `readText`), and clears
      the pending path so it isn't re-opened.
- [ ] **Permission-denied save**: given a write that throws a permission-denied
      `SftpException` while connected, `saveNow()` yields `SaveStatus.Error` and does
      **not** upsert a `PendingEdit`.
- [ ] **Connectivity failure still caches**: a generic write failure / disconnected
      state still falls back to `PendingSync` (regression guard for 014 behavior).

## Integration / UI
- [ ] **Full flow (manual)**: long-press file → menu → Copy path → snackbar +
      clipboard; long-press editable file → Edit → Editor tab opens file; Select →
      multi-select bar.

## Edge Cases
- [ ] In multi-select mode long-press toggles (no menu).
- [ ] Edit hidden for directories and non-editable files.
</content>
