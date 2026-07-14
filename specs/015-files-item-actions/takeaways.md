# Files Item Actions (Long-Press Context Menu) — Takeaways

## What went well
- Layered the feature as shared scaffolding (context menu) → two clean vertical
  slices (copy-path, edit-in-editor), each an independently building/testing commit.
- Reused established patterns wholesale: `DropdownMenu` from `EditorScreen.LocationRow`,
  the `KnownHostsStore` interface + `InMemory*` double for the new `Clipboard`, and
  the `@Singleton SessionState` bridge for cross-tab (Files → Editor) hand-off.
- Kept all new logic JVM-unit-testable — no Mockito mocks needed for the new paths
  (`InMemoryClipboard`, pure `EditableFileType`, `FakeSftpClient`).

## What we learned / surprises
- Long-press already had a job (multi-select). Migrating it into a menu **Select**
  item, while keeping tap/long-press toggling *inside* multi-select mode, preserved
  the existing UX with no regression to the multi-select tests.
- `MutableSharedFlow` (no replay) races the test collector: emit before the collector
  subscribes → lost event. Fix in tests: subscribe, `advanceUntilIdle()`, then act.
- SFTP can't reliably predict write access (no whoami/uid/groups), so we intentionally
  do **not** gate Edit on permissions — only on file *type* — and surface write
  failures at save time (matches vim / VS Code Remote behavior).
- Found a latent 014 gap: `EditorViewModel.doSave` cached **any** save failure as
  pending-sync, including permission-denied — which would silently hoard an
  unsyncable edit. Now split: permission-denied → `SaveStatus.Error`; connectivity
  failure → offline cache.

## Reusable patterns
- `Clipboard` interface + `AndroidClipboard` + `InMemoryClipboard` + `@Binds` — the
  KnownHostsStore recipe applied to a system service, keeping copy logic testable.
- Per-row `DropdownMenu` inside a `Box` wrapping the row, opened from
  `combinedClickable(onLongClick)` only when not already in a selection mode.
- Cross-tab open: stash a value in `SessionState` + emit a navigate event; the target
  screen consumes-and-clears it in a `LaunchedEffect(Unit)` on entry.

## Follow-ups (deferred)
- Capture POSIX mode/owner into `RemoteFile` to show a read-only banner or a lock
  badge (Option B). Not needed for correctness; save-time errors already cover it.
</content>
