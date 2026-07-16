# Session Health Monitoring — Test Plan

## Unit Tests

### JschSftpClientTest
- [x] **Dead session flips connected**: Arrange mock JSch Session with `isConnected=false`,
      inject via reflection into `JschSftpClient`. Act call `homeDirectory()` (which
      goes through `openChannel()`). Assert connected StateFlow is now `false`.
  - Covered by: `JschSftpClientTest.kt` `openChannel throws and flips connected to false when session is non-null but dead`

## Integration / UI
- Manual: connect, switch phones to airplane mode, switch back, observe that the
  Files tab shows an error (not a crash or spurious navigation) and the Editor shows
  "Not connected" on the next save attempt. Reconnect and verify Editor auto-flushes
  any cached edit.
