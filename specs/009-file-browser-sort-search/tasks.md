# Files Page: Hidden Toggle, Sort & Search — Tasks

## Block 0: Spec & Design (before code)
- [x] spec.md + plan.md drafted
- [x] Mockup: none needed (additive Files-screen changes)
- [x] test_plan.md documented

## Block 1: View logic
- [ ] **Task 1.1**: `FileView` (`SortMode` enum + pure `apply`) + `FileViewTest` — `ui/files/FileView.kt`, `test/.../FileViewTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: ViewModel
- [ ] **Task 2.1**: `FilesViewModel` raw/derived list, view inputs, setters, `loadFiles` via `FileView`, navigate clears search; update + extend VM test — `ui/files/FilesViewModel.kt`, `test/.../FilesViewModelTest.kt`
  - Tests: `./gradlew testDebug`

## Block 3: UI
- [ ] **Task 3.1**: `FilesScreen` search row + sort/hidden `DropdownMenu` + adaptive empty-state — `ui/files/FilesScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 4: Ship
- [ ] **Task 4.1**: takeaways → MEMORY, `specs/index.md` (009 ✅), README feature bullet
