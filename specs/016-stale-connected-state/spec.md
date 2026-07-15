# Session Health Monitoring & Stale Connected State — Specification

## Bug Description
Two related issues:
1. The SFTP connection appears to drop "instantly" when switching tabs — the Files tab
   shows "Not connected" and navigates to the Connect tab even though the session was
   just established.
2. The Editor tab save always falls through to offline cache (`PendingSync`), requiring
   a manual reconnect from the Connect tab to flush.

## Root Cause
Two defects in `JschSftpClient`:

1. **`session` field not `@Volatile`.** Writes to `session` on one `Dispatchers.IO`
   thread could be invisible to reads on another IO thread across coroutine
   invocations. This caused `openChannel()` to read `null` immediately after a
   successful `connect()`, manifesting as an instant "connection lost" on tab switch.

2. **No dead-session detection.** The `connected` StateFlow in `SessionState` is set
   manually — only by `disconnectInternal()` (called from `connect()` and
   `rejectKey()`). When the JSch session dies silently (network change, server
   idle-timeout, Android Doze), `connected` stays `true`. The Editor's `doSave()`
   checks `connected`, sees `true`, tries `writeText()`, fails, and caches offline —
   even though a fresh `connect()` would work. The session death was invisible until
   an operation failed, and even then, nobody updated the stale flag.

## Fix
1. **`@Volatile` on `session`** — guarantees cross-thread visibility of the session
   field on the `@Singleton` object accessed from multiple `Dispatchers.IO` coroutines.
2. **Dead-session check in `openChannel()`** — before opening a channel (and after
   failure to open one), check `session.isConnected`. If false, call
   `disconnectInternal()` to sync the `connected` StateFlow. All 10 SFTP operations
   go through `openChannel()`, so this single change self-heals on the first failed
   operation.

The Editor's existing `onConnectedChanged` (014) auto-flushes pending edits on
reconnect. The Files tab's epoch-based `onEnterScreen()` resets to the initial
directory on reconnect. No ViewModel changes needed — the existing reconnect flows
are correct; they just weren't being triggered because `connected` was stale.

## Acceptance Criteria
- [ ] After a successful `connect()`, switching to the Files tab within the same
      composition cycle shows the file listing (no spurious "Not connected").
- [ ] When the JSch session dies silently while the user is on another tab, the
      first SFTP operation detects it, flips `connected` to `false`, and the Editor
      shows "Not connected" (not a misleading "saved" or pending-sync from a failed
      write).
- [ ] Reconnecting from the Connect tab triggers the Editor's pending-edit flush (014
      behavior, verified by existing tests).
- [ ] Reconnecting triggers the Files tab's epoch-based reload (010 behavior, verified
      by existing tests).

## Non-Functional
- No polling, no keepalive thread, no new background work.
- Detection latency: one SFTP operation (the first after the session dies). That
  operation fails, but the flag is corrected so subsequent operations see reality.

## Out of Scope
- Auto-reconnect on dead session.
- Proactive session health monitoring (heartbeat).

## Connect Tab UI (connected/disconnected states)
- [ ] When connected (`connected == true`), all input fields (host, port, username,
      password, default directory) are disabled/greyed out, the recent connections
      dropdown is disabled, and the save-credentials switch is disabled.
- [ ] When connected, the button text is **Disconnect** with a red/error color.
- [ ] When disconnected (`connected == false`), all fields are editable and the
      button text is **Connect** with the default green/primary color.
- [ ] Tapping Disconnect calls `sftpClient.disconnect()`; `connected` flips to
      `false` in SessionState, which the ViewModel's collector propagates to UI state
      within the same frame.
