# Editor First-Open Race — Takeaways

## What went well
- The fix stayed in one file (`EditorViewModel.kt`): authoritative gate, one extra
  catch, a `loaded` flag, and a reconnect recovery — no protocol, session, or UI
  changes.
- The reported workaround ("disconnect and retry once or twice") was the strongest
  diagnostic clue: a reconnect re-fires the `connected` StateFlow `false → true`,
  which re-syncs whatever state the open decision depended on. Any fix had to make
  that recovery automatic — which the `onConnectedChanged` reload does.

## What we learned / surprises
- **`uiState.connected` is a mirror, not the source of truth.** The open decision
  was gated on a VM-local copy updated by a collect coroutine; the decision read
  happened asynchronously after a Room suspension, with no ordering guarantee.
  `StateFlow.value` is the authoritative read — always current, thread-safe — and
  the mirror should only drive UI affordances, never control flow.
- **A one-shot decision with no recovery is the real defect.** Whether the stale
  snapshot was the trigger in every case or just one interleaving, the code had no
  path out of the `NotConnected` branch once taken. Self-healing on the
  `false → true` transition closes that class of bug entirely.
- **`openChannel()` throws raw `IllegalStateException`**, not `SftpException`.
  `open()` only caught `SftpException`, so a snapshot-`true` + dead-session read
  would have crashed. Worth remembering when touching `JschSftpClient` in future.
- **Guarding the recovery needed a `loaded` flag.** Content-empty can't
  distinguish "never loaded" from "legitimately empty file" or "user deleted
  everything"; the flag is unambiguous and cheap.

## Reusable patterns
- Gate decisions on the authoritative source (`StateFlow.value`), keep derived
  mirrors for display.
- Any UI state that degrades permanently from a transient condition needs a
  recovery transition (here: reconnect `false → true`) — never a silent dead end.
- Catch the full exception surface of the layer you call (JSch's raw
  `IllegalStateException` for not-connected), or crash.

## Known gaps / future work
- The Files → Editor handoff (`SessionState.pendingEditPath` + `LaunchedEffect`
  consume) remains fire-and-forget; converting the pending path to a
  `StateFlow<String?>` collected by the EditorVM would remove the last
  ordering-sensitive piece of the cross-tab bridge.
- `JschSftpClient.openChannel()` could throw a typed `SftpException` instead of
  `IllegalStateException` so every caller handles one exception type.
