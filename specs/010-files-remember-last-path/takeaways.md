# Files Page: Remember Last Visited Path — Takeaways

## What Went Well
- The fix was small because `FilesViewModel` is already Activity-scoped — its state
  survives tab switches; only the `LaunchedEffect(Unit)` reset had to change.
- A single `SessionState.epoch` cleanly separates "new connection" from "returning to
  the tab," reusing the existing Connect→Files bridge with no new wiring.

## What We Learned
- In a no-NavHost tab app, `when(destination)` removes/re-adds screen composables, so
  `LaunchedEffect(Unit)` re-fires on every tab re-entry — route (re)loads through an
  explicit `onEnterScreen()` rather than resetting state in the effect.
- Detect a new session by an epoch counter, not by comparing the resolved initial
  directory (which can repeat across hosts/defaults).

## API / Tech Surprises
- None — pure state-management change; no new dependencies or APIs.

## Patterns Worth Reusing
<!-- These get promoted to root MEMORY.md after ship -->
- "Remember last path across tab switches" via `SessionState.epoch` + `onEnterScreen()`
  (see MEMORY #ui / "Patterns That Worked" and ADR-009).
