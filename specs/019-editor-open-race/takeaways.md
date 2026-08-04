# Editor First-Open Race — Takeaways

## What went well
- **The device told us where to look.** The revised symptom (Editor shows the
  locations list — `openLocation == null`) plus "plain retry still fails, only
  reconnect works" pointed at the *handoff*, not the open decision. v1 had
  hardened the wrong layer; the reactive bridge fixed the actual failure.
- **A StateFlow bridge is the right primitive here.** `SessionState.pendingEditPath`
  as `StateFlow<String?>` + an EditorVM init collect gives replay-to-new-subscribers
  (VM created after the path is set) and push (VM already exists) — both ordering
  cases covered with no composition timing. It mirrors the existing `connected`
  pattern, so it reads naturally.
- **The `navigateToEditor` event now only switches tabs.** The file-open no longer
  depends on it, so even a dropped SharedFlow event is self-healing.

## What we learned / surprises
- **`LaunchedEffect(Unit)` + a plain mutable field is a fragile cross-VM bridge.**
  The consume relies on the screen re-entering composition at the right moment
  relative to the write. It worked in tests (which call the consume directly) and
  even on some devices, but not reliably in production — the classic
  "untested composition-order" gap. The cross-VM handoff test (FilesVM.editFile →
  EditorVM opens) is the coverage that was missing.
- **v1's connected-gate fix was still necessary** (authoritative `StateFlow.value`
  read, `IllegalStateException` catch, reconnect self-heal) — it removes a latent
  crash and a stale-snapshot race — but it could not fix a file that never opened.
- **StateFlow conflates** — rapid Edit taps on A then B keep only B. That's
  desirable ("open what I last tapped") but worth remembering for any bridge.

## Reusable patterns
- Cross-ViewModel handoffs in a no-NavHost tab app: a `StateFlow` on the shared
  singleton + a collect in the receiving VM (same as `connected`), instead of a
  plain var + screen-entry `LaunchedEffect`.
- When a bug "needs a reconnect to fix", look for what the reconnect forces to
  happen (fresh screen re-entry / state re-emission) and make that event-driven
  instead of timing-dependent.

## Known gaps / future work
- `navigateToEditor` is still a replay=0 SharedFlow; with the reactive bridge a
  dropped event only delays the tab switch (the file opens regardless). Bumping to
  replay=1 would also restore the tab switch — optional.
- `JschSftpClient.openChannel()` still throws raw `IllegalStateException` for a
  dead session; a typed `SftpException` would unify caller handling.
