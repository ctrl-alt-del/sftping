# Connection Status Indicator — Test Plan

## Unit Tests

### FilesViewModelTest
- [x] **`connected reflects sessionState`**: Arrange VM with `SessionState()`,
      advance to let collector subscribe. Act: `setConnected(true)` then
      `setConnected(false)`. Assert `uiState.connected` tracks each change
      synchronously (StateFlow emits on UnconfinedTestDispatcher).

### TransfersViewModel
- No new unit test (no TransfersViewModelTest exists in the project). The
  ViewModel is a thin wrapper around TransferManager; `connected` collection
  is verified by manual app testing.

### EditorViewModelTest
- No change needed — Editor already has `connected` collection tested
  indirectly via save-status tests (`SaveStatus.NotConnected`, etc.).

## Integration / Manual
- [ ] Connect via Connect tab → switch to Files → green bar visible.
- [ ] Switch to Transfers → green bar visible (even if list is empty).
- [ ] Switch to Editor → green bar visible (location list and editor pane).
- [ ] Disconnect (tap red Disconnect button) → all tabs show red bar.
- [ ] Reconnect → all tabs show green bar again.

## Edge Cases
- [ ] Rapid connect/disconnect/reconnect → bar color updates without flicker.
