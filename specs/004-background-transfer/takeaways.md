# Background Transfer — Takeaways

## What Went Well
- WorkManager + HiltWorker integration was straightforward: `@HiltWorker` + `Configuration.Provider`
  in the Application + disabling the default initializer in the manifest.
- Reactive DAO (`observeAll(): Flow`) combined with `stateIn()` eliminates manual state
  synchronization between Worker writes and TransferManager reads. The DAO is the single
  source of truth.
- Foreground service with `dataSync` type + notification channel works on targetSdk 36
  with proper permissions declared in the manifest.

## What We Learned
- `CoroutineWorker`'s `context` parameter shadows the `android.content.Context` import in
  the class body; use `applicationContext` instead to avoid ambiguity or rename the
  constructor parameter.
- Room's `suspend` DAO methods cannot be called from non-suspend callbacks (like JSch
  progress monitors). In the Worker, skip intermediate progress persistence and rely on
  final status updates via `Result.success/failure`.
- `StateFlow.first()` returns the current cached value immediately, not the next emission.
  To wait for a state change, use `first { predicate }` which suspends until the predicate
  is met.
- `WorkManager.enqueue()` with `setConstraints(NetworkType.CONNECTED)` ensures the worker
  only runs when the device is online. Combined with `setForeground()`, the transfer
  survives app backgrounding.

## Patterns Worth Reusing
- **Reactive TransferManager**: DAO → Flow → stateIn → StateFlow for both UI and Worker
  consumption. No manual polling, no explicit synchronization.
- **Pause = cancelWork + persist offset; Resume = re-enqueue from offset**: this avoids
  Worker time limits and `while(isPaused) delay()` anti-patterns.
- **Notification as progress UI**: the foreground notification provides updates
  independently of the Compose UI, making it the primary progress channel for background
  transfers. The TransfersScreen is a secondary view.
