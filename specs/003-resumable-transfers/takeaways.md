# Resumable Transfers — Takeaways

## What Went Well
- Room DAO suspend functions integrated cleanly with the TransferManager; the DAO serves as
  both the transfer state source-of-truth and the bridge for resume offsets.
- JSch `ChannelSftp.RESUME` mode handles remote-side skipping automatically — no need to
  manually manage byte offsets at the SFTP protocol level. For download, `get(src, dst,
  monitor, RESUME)` checks local file length; for upload, `put(stream, dst, monitor, RESUME)`
  checks remote file length. InputStream must be manually skipped for efficiency (avoids
  re-reading already-uploaded bytes).
- The `PAUSED` status propagates cleanly through the stack: `TransferTaskStatus.PAUSED` (Room)
  → `TransferStatus.PAUSED` (UI) → TransfersScreen renders it with a Pause icon and
  "Paused · X of Y" label.

## What We Learned
- Room suspend DAO functions cannot be called from non-suspend callbacks (like
  `SftpProgressMonitor` → `(Long, Long) -> Unit` progress lambdas). Wrap them in
  `scope.launch { dao.updateProgress(...) }` to bridge the suspend gap.
- Compose `when` expressions must be exhaustive for sealed/enum types; adding `PAUSED` to
  `TransferStatus` broke TransfersScreen's `statusText()` and icon `when` until branches
  were added.
- `runTest` with a `CoroutineScope(Dispatchers.IO)` inside the subject under test does NOT
  use virtual time — `delay()` calls block real time. This makes async state transitions
  in TransferManager's internal scope non-deterministic in tests.
- `ChannelSftp.setBufferSize()` doesn't exist in the mwiede JSch fork we're using; the
  buffer size is a connection-level config (`sftp_buffer_size`). We deferred the 1MB
  buffer tuning to later.

## Patterns Worth Reusing
- **Room-backed state holder**: TransferManager persists task state to Room on every
  significant change (start, progress every ~1MB, pause, complete/fail). On creation,
  it loads all from DAO and maps to UI model. This pattern will feed directly into the
  WorkManager-based re-enqueue model for 004.
- **Fake DAO for tests**: a simple `ConcurrentHashMap`-backed `FakeDao` implementation
  of `TransferTaskDao` provides full API coverage without Room's full database machinery.
  Works for both JVM unit tests and async flows.
