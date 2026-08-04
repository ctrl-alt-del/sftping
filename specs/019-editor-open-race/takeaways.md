# Editor First-Open Race — Takeaways

## What went well
- **The device told us where to look — three times.** (1) "Cannot edit" → open
  decision; (2) "shows no saved locations" → the handoff; (3) "file opens but not
  editable" → the editor trusts a manual `connected` flag that can disagree with
  the real session. Each iteration narrowed the same flow until the remaining
  dependency (the flag) was removed.
- **A StateFlow bridge is the right primitive for cross-VM handoffs.**
  `SessionState.pendingEditPath` as `StateFlow<String?>` + an EditorVM init
  collect gives replay-to-new-subscribers (VM created after the path is set) and
  push (VM already exists) — both ordering cases covered with no composition
  timing.
- **Trust the operation result, not a state flag.** After the session-health
  saga, `open()` now just attempts the read and maps the outcome. A stale flag can
  no longer lock the editor (stale-false) or crash it (stale-true).
- **Keepalives belong on the client.** 30 s was longer than typical mobile
  NAT/proxy idle timeouts; 10 s + countMax 3 keeps the session alive through
  browsing pauses and detects deaths faster.

## What we learned / surprises
- **`LaunchedEffect(Unit)` + a plain mutable field is a fragile cross-VM bridge.**
  The consume relies on the screen re-entering composition at the right moment
  relative to the write. The cross-VM handoff test (FilesVM.editFile → EditorVM
  opens) is the coverage that was missing.
- **The `connected` flag is a hint, not a contract.** 016 made it accurate on
  dead-session detection, but it can still lag reality (idle drops aren't noticed
  until the next operation). Gating editability on it produced a locked editor
  while the user believed they were connected.
- **v1's `editable` requiring `loaded` was over-strict** — any failed load locked
  the field even while connected. The 014 offline-cache design already supports
  editing without a live connection; the gate contradicted it.

## Reusable patterns
- Cross-ViewModel handoffs in a no-NavHost tab app: a `StateFlow` on the shared
  singleton + a collect in the receiving VM (same as `connected`), never a plain
  var + screen-entry `LaunchedEffect`.
- When a bug "needs a reconnect to fix", look for what the reconnect forces to
  happen (fresh screen re-entry / state re-emission / new session) and make that
  event-driven or flag-independent instead of timing-dependent.
- Client keepalive interval must be shorter than the network's idle-drop window.

## Known gaps / future work
- Auto-reconnect on dead session is still deferred (016); the v1 self-heal reloads
  a stuck open on the next reconnect.
- `navigateToEditor` is still a replay=0 SharedFlow; with the reactive bridge a
  dropped event only delays the tab switch.
- `JschSftpClient.openChannel()` still throws raw `IllegalStateException` for a
  dead session; a typed `SftpException` would unify caller handling.

