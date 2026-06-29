# Connect Page: Password Visibility & Default Directory — Test Plan

## Unit Tests

### ConnectionProfile (JVM)
- [ ] **Round-trip with defaultDirectory**: Arrange a profile with
  `defaultDirectory = "/var/www"`; Act `toJson`/`fromJson`; Assert equality.
- [ ] **Missing field default**: Arrange JSON without `defaultDirectory`; Act
  `fromJson`; Assert `""`.
- [ ] **Existing round-trip tests still pass** (defaultDirectory defaults to "").

### ConnectionViewModel (JVM)
- [ ] **updateDefaultDirectory**: Act `updateDefaultDirectory("/srv")`; Assert
  `uiState.defaultDirectory == "/srv"`.
- [ ] **blank dir → home on connect**: Arrange `connect` returns `Trusted`,
  `homeDirectory()` returns `/home/admin`, dir blank; Act `connect()`; Assert
  `sessionState.initialDirectory == "/home/admin"`.
- [ ] **explicit dir on connect**: Arrange dir = `/var/www`, `Trusted`; Act
  `connect()`; Assert `sessionState.initialDirectory == "/var/www"` and
  `homeDirectory()` not consulted.
- [ ] **home resolution failure → "/"**: Arrange dir blank, `homeDirectory()`
  throws; Act `connect()`; Assert `sessionState.initialDirectory == "/"`.
- [ ] **selectRecent prefills dir**: Arrange profile with `defaultDirectory`; Act
  `selectRecent`; Assert field populated.
- [ ] **existing VM tests pass** with the new `SessionState` constructor arg.

### FilesViewModel (JVM)
- [ ] **loadFiles seeds from SessionState**: Arrange `sessionState.initialDirectory
  = "/home/user"`; Act `vm.loadFiles()` (no arg); Assert `currentPath ==
  "/home/user"` and `listFiles("/home/user")` invoked.
- [ ] **existing Files tests pass** with the new `SessionState` constructor arg.

## Integration / UI (manual)
- [ ] Toggle eye → password reveals/masks; hidden in key-auth mode.
- [ ] Connect with blank dir → lands in home; with explicit dir → lands there;
  bad dir → "List failed" shown, stays.

## Edge Cases
- [ ] Home resolution returns blank/null/throws → `/`.
- [ ] Two recents with different default dirs → each recalls its own.
