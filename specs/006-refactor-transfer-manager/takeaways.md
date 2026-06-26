# TransferManager Refactor — Takeaways

## What Went Well
- The Strategy + UseCase pattern decoupled TransferManager from JSch-specific code: the manager
  now has zero imports from `com.jcraft.jsch` and delegates all business logic.
- `callbackFlow` bridges JSch's callback-based progress to `Flow<TransferProgress>` cleanly.
  Each `trySend` pushes progress to the downstream collector without blocking the JSch thread.
- Adding `TransferDirection.toTaskDirection()` on the enum itself eliminated the private
  extension function in TransferManager that was causing ambiguity.

## What We Learned
- Room KSP overrides default parameter values — always use **named parameters** when
  constructing `TransferTask` in test code, even for fields with defaults.
- `mockito-kotlin`'s `doAnswer` with `invocation.getArgument(index)` needs the correct
  parameter index (0-based). The `downloadWithResume` callback is argument 3, not 2.
- `mock<Context>()` returns null for `cacheDir` — must `doReturn(realDir).when(context).cacheDir`
  for any test path that touches `context.cacheDir`.
- The `@Binds @Singleton` pattern with `@Inject @Singleton` on the implementation class
  is the standard Hilt binding; no duplicate binding errors arise.

## Patterns Worth Reusing
- **Strategy + UseCase sandwich**: `Worker → UseCase → Strategy → Transport`. The UseCase
  handles business rules (offset, retries, persistence); the Strategy handles protocol
  specifics (JSch, FTP, SCP); the Worker handles Android lifecycle (foreground service,
  notifications). Each layer is independently testable.
- **Flow-based progress**: replacing callback-driven progress with `Flow<TransferProgress>`
  allows the DAO layer to `collect` progress updates in a structured way, making the
  refactored code more composable.
- **Enum extension for type mapping**: `TransferDirection.toTaskDirection()` and
  `TransferTaskDirection.toTransferDirection()` (inlined) keep the bidirectional mapping
  close to the types, avoiding scattered when-expressions.
