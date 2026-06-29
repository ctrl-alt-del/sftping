# Files Page: Remember Last Visited Path — Test Plan

## Unit Tests

### ConnectionViewModel (JVM)
- [ ] **connect bumps epoch**: Arrange `Trusted` connect; Act `connect()`; Assert
  `sessionState.epoch` incremented (and `initialDirectory` still set — 008 intact).

### FilesViewModel (JVM)
- [ ] **first enter loads initial directory**: Arrange `sessionState.epoch=1`,
  `initialDirectory="/home/u"`; Act `onEnterScreen()`; Assert `currentPath ==
  "/home/u"` and `listFiles("/home/u")` invoked.
- [ ] **same-session re-entry reloads last path**: Arrange enter at `/home/u`, then
  `navigateTo("logs")` → `/home/u/logs`; Act `onEnterScreen()` again (epoch
  unchanged); Assert `currentPath == "/home/u/logs"` (not reset to home).
- [ ] **new connection resets to initial directory**: Arrange entered at
  `/home/u/logs`; bump `sessionState.epoch` and change `initialDirectory="/srv"`;
  Act `onEnterScreen()`; Assert `currentPath == "/srv"` and `pathStack` empty.
- [ ] **existing tests still pass** (loadFiles/navigate/sort/search unaffected).

## Integration / UI (manual)
- [ ] Browse into a folder → switch to Transfers → back to Files → same folder
  shown (re-listed). Reconnect → Files opens home.

## Edge Cases
- [ ] Reconnect to a different host → no stale path; resets to new home.
- [ ] Back-stack preserved on same-session re-entry; cleared on new connection.
