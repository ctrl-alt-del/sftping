# Files Item Actions (Long-Press Context Menu) — Tasks

## Block 0: Spec & Design (before code)
- [x] `doc-coauthoring`: spec.md + plan.md approved
- [x] Mockup: skipped (additive Files change, per 009/011 precedent)
- [x] `test_plan.md`: test scenarios documented

## Block 1: Context menu scaffolding

- [x] **Task 1.1**: Replace long-press `toggleSelection` with a per-row `DropdownMenu`
      containing a **Select** item (no behavior lost) — `ui/files/FilesScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: Copy path

- [x] **Task 2.1**: Add `Clipboard` interface + `AndroidClipboard` impl +
      `InMemoryClipboard` double + Hilt `@Binds` — `util/Clipboard.kt`,
      `di/ClipboardModule.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 2.2**: `FilesViewModel.copyPath(file)` + `message` SharedFlow; wire
      **Copy path** menu item + snackbar — `ui/files/FilesViewModel.kt`,
      `ui/files/FilesScreen.kt`; add `copyPath` unit tests
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 3: Edit in editor

- [x] **Task 3.1**: Pure `EditableFileType.isEditable(name)` allowlist + unit tests —
      `ui/files/EditableFileType.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 3.2**: `SessionState.pendingEditPath`; `EditorViewModel` opens it
      transiently + permission-denied save-error mapping; `FilesViewModel.editFile`;
      conditional **Edit** menu item; `MainActivity` `onNavigateToEditor` — 
      `sftp/SessionState.kt`, `ui/editor/EditorViewModel.kt`,
      `ui/files/FilesViewModel.kt`, `ui/files/FilesScreen.kt`, `MainActivity.kt`;
      unit tests for editFile, pending-path consume, save-error mapping
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 4: Ship
- [x] **Task 4.1**: takeaways.md; promote to MEMORY.md; update AGENTS.md, README.md,
      specs/index.md; flip plan.md status → ✅ Done
</content>
