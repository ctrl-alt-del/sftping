---
feature_id: "018"
name: "Connection Status Indicator"
status: "✅ Done"
depends_on: ["001", "008", "016"]
touches:
  - "ui/components/ConnectionIndicator.kt"
  - "ui/files/FilesScreen.kt"
  - "ui/files/FilesViewModel.kt"
  - "ui/transfers/TransfersScreen.kt"
  - "ui/transfers/TransfersViewModel.kt"
  - "ui/editor/EditorScreen.kt"
  - "AGENTS.md"
  - "MEMORY.md"
  - "README.md"
  - "specs/index.md"
created: "2026-07-17"
---

# Connection Status Indicator — Plan

## Approach

Add a thin 4dp colored bar with a dot above the TopAppBar on Files, Transfers,
and Editor tabs. The bar is driven by `SessionState.connected` (StateFlow),
collected by each ViewModel and mapped to UI state. No polling, no extra SFTP
operations.

A shared `ConnectionIndicator` composable lives in `ui/components/` so all
three tabs use the same rendering logic.

### Data flow

```
JschSftpClient → SessionState.connected (StateFlow<Boolean>)
    ├── FilesViewModel.collect → FilesUiState.connected
    ├── TransfersViewModel.collect → _connected (NEW)
    └── EditorViewModel.collect → EditorUiState.connected (exists)
            → Composable → ConnectionIndicator(isConnected) → green/red bar
```

### Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `ui/components/ConnectionIndicator.kt` | Shared green/red bar composable |
| Change | `ui/files/FilesViewModel.kt` | Add `connected` to UI state + collect |
| Change | `ui/files/FilesScreen.kt` | Add indicator to topBar Column |
| Change | `ui/transfers/TransfersViewModel.kt` | Inject SessionState, expose `connected` |
| Change | `ui/transfers/TransfersScreen.kt` | Fix empty-state early return, add indicator |
| Change | `ui/editor/EditorScreen.kt` | Add indicator to LocationsList + EditorPane |

## Risks

- Transfers empty-state early return skipped the Scaffold entirely, meaning
  the indicator was invisible when the list was empty. Fixed by moving the
  empty state inside a Scaffold with a topBar containing the indicator.
- Nothing else — this is a pure UI addition with no logic changes to the
  connection or transfer subsystems.

## Dependencies

001 (connection), 008 (SessionState), 016 (dead-session detection makes
connected reliable).
