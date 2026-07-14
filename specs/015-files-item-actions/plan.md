---
feature_id: "015"
name: "Files Item Actions (Long-Press Context Menu)"
status: "✅ Done"
depends_on: ["009", "014"]
touches:
  - "ui/files/FilesScreen.kt"
  - "ui/files/FilesViewModel.kt"
  - "ui/files/EditableFileType.kt"
  - "ui/editor/EditorViewModel.kt"
  - "sftp/SessionState.kt"
  - "sftp/Clipboard.kt"
  - "MainActivity.kt"
  - "di/ClipboardModule.kt"
created: "2026-07-14"
---

# Files Item Actions (Long-Press Context Menu) — Plan

## Approach

Replace the Files list's long-press → `toggleSelection` behavior with a per-row
Material 3 `DropdownMenu` (same pattern as `EditorScreen.LocationRow`). The menu
offers **Copy path**, **Edit** (conditional), and **Select**. Multi-select is now
entered via the **Select** item; once active, tap/long-press keep toggling and no
menu is shown.

- **Copy path**: `FilesViewModel.copyPath(file)` writes `file.path` to the system
  clipboard via a `Clipboard` abstraction (interface + Android impl + Hilt `@Binds`
  + `InMemoryClipboard` test double, mirroring `KnownHostsStore`). A one-shot
  `SharedFlow<String>` (`message`) drives a "Path copied" snackbar.
- **Edit**: gated by a pure `EditableFileType.isEditable(name)` text-extension
  allowlist. `FilesViewModel.editFile(file)` stashes the path in
  `SessionState.pendingEditPath` and emits a navigate-to-editor event; `MainActivity`
  switches to the Editor tab via a new `onNavigateToEditor` callback. `EditorViewModel`
  consumes `pendingEditPath` (on connection/init) and opens it transiently via
  `EditorLocation.of(remotePath = ...)` — not persisted.
- **Permission handling**: no pre-check. Improve `EditorViewModel.doSave` to map a
  permission-denied `SftpException` to `SaveStatus.Error`, distinct from the offline
  cache fallback (which stays for connectivity loss). This fixes a 014 gap where any
  write failure was cached as pending-sync.

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `sftp/Clipboard.kt` | `Clipboard` interface + `AndroidClipboard` impl + `InMemoryClipboard` |
| Create | `di/ClipboardModule.kt` | Hilt `@Binds` for `Clipboard` |
| Create | `ui/files/EditableFileType.kt` | Pure text-extension allowlist |
| Change | `ui/files/FilesViewModel.kt` | `copyPath`, `editFile`, `message` SharedFlow |
| Change | `ui/files/FilesScreen.kt` | Per-row DropdownMenu; wire actions + snackbar + nav |
| Change | `sftp/SessionState.kt` | `pendingEditPath` bridge field |
| Change | `ui/editor/EditorViewModel.kt` | Consume `pendingEditPath`; save-error mapping |
| Change | `MainActivity.kt` | `onNavigateToEditor` callback |

## Risks

- Changing long-press semantics could confuse existing muscle memory; mitigated by
  keeping toggle behavior once multi-select is active and adding **Select** to the menu.
- Detecting "permission denied" from JSch relies on the wrapped `SftpException`
  message/cause; mitigated by matching on the JSch status/cause conservatively and
  defaulting to the existing offline-cache path when unsure.

## Dependencies

- 009 (Files browser structure), 014 (Editor open/transient location, SessionState
  connected flow).
</content>
