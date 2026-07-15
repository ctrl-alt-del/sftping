---
feature_id: "016"
name: "Session Health Monitoring & Stale Connected State"
status: "✅ Done"
depends_on: ["001", "008", "010", "014"]
touches:
  - "sftp/JschSftpClient.kt"
  - "sftp/SessionState.kt"
  - "ui/connection/ConnectionScreen.kt"
  - "ui/connection/ConnectionViewModel.kt"
created: "2026-07-15"
---

# Session Health Monitoring — Plan

## Approach

Two targeted fixes in `JschSftpClient`, no ViewModel changes needed:

1. **`@Volatile` on the `session` field** — the `@Singleton`'s `session` field is
   written and read across different `Dispatchers.IO` coroutine invocations. Without
   `@Volatile`, writes may not be visible to reads, causing `openChannel()` to throw
   `IllegalStateException("Not connected")` immediately after `connect()`. One-line fix.

2. **Dead-session detection in `openChannel()`** — after the null check, and in the
   catch block for failed channel open, call `session.isConnected`. If false,
   `disconnectInternal()` syncs the `connected` StateFlow. This is the single
   entry point for all 10 SFTP operations, so one change covers every caller.

No FilesViewModel or EditorViewModel changes. The existing reconnect flows
(epoch-based reload in 010, auto-flush in 014) are correct — they just weren't
triggered because `connected` was stale.

## Files Changed

| Action | File | Rationale |
|--------|------|-----------|
| Change | `sftp/JschSftpClient.kt` | `@Volatile` + dead-session check in `openChannel()` |
| Change | `ui/connection/ConnectionViewModel.kt` | `connected` StateFlow collection, `disconnect()` method |
| Change | `ui/connection/ConnectionScreen.kt` | Disable fields when connected, red Disconnect button |
| Change | `sftp/SessionState.kt` | (no change; documentation only — `connected` is now actually accurate) |

## Risks

- `session.isConnected` may return `true` for a brief window after a graceful
  server disconnect when the SSH transport is dead but the JSch object hasn't
  noticed. Mitigation: the next operation throws, the catch block in `openChannel()`
  re-checks `isConnected` and disconnects. Worst case: one extra stale operation.
- `disconnectInternal()` sets `session = null`. If called from within an operation
  that later needs `session` in its `finally` block, NPE. Mitigation:
  `disconnectInternal()` is only called in the error path; the `finally` blocks
  close the channel (which was already attempted/failed) and don't touch `session`.

## Dependencies

001 (connection), 008 (SessionState), 010 (epoch-based Files reload), 014 (Editor
reconnect-flush).
