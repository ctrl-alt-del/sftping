# Connection Status Indicator — Tasks

## Block 0: Spec & Design
- [x] `doc-coauthoring`: spec.md + plan.md
- [x] Mockup: generated (`ux-ui/mockup.png`)
- [x] `test_plan.md`: test scenarios documented

## Block 1: Implementation
- [x] **Task 1.1**: Create `ConnectionIndicator` shared composable —
      `ui/components/ConnectionIndicator.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 1.2**: Collect `connected` in FilesViewModel + add indicator to
      FilesScreen — `ui/files/FilesViewModel.kt`, `ui/files/FilesScreen.kt`;
      test `connected` collection in FilesViewModelTest
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 1.3**: Inject SessionState into TransfersViewModel, fix empty-state
      early return, add indicator — `ui/transfers/TransfersViewModel.kt`,
      `ui/transfers/TransfersScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 1.4**: Add indicator to Editor tab (LocationsList + EditorPane) —
      `ui/editor/EditorScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: Docs
- [x] **Task 2.1**: takeaways.md; promote to MEMORY.md; update AGENTS.md,
      README.md, specs/index.md; flip plan.md status → ✅ Done
