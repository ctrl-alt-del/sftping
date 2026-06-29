# Persist Host-Key Trust & Revoke — Takeaways

## What Went Well
- The existing `ConnectionRepository`/`ConnectionProfile` split (DataStore I/O vs.
  pure JSON model) was a clean template for `DataStoreKnownHostsStore`/`TrustedHost`.
- Making `KnownHostsStore` suspend was transparent: every caller in `JschSftpClient`
  already ran inside `withContext(Dispatchers.IO)`.

## What We Learned
- Host-key fingerprints are **public keys, not secrets** — persist them as plaintext
  JSON in DataStore; the Android Keystore is reserved for credentials.
- Keeping an `InMemoryKnownHostsStore` that implements the same suspend interface
  gives a fast JVM unit-test double without needing DataStore on a device.

## API / Tech Surprises
- `AlertDialog` only exposes confirm + dismiss slots; a third action
  ("Revoke & re-verify") lives inside the dialog's `text` content as a `TextButton`.
- "Revoke & re-verify" reuses the normal connect path: remove the stored key, then
  re-run `connect()` — it naturally returns `Unknown` once the stored entry is gone,
  so no special re-verification branch is needed.

## Patterns Worth Reusing
<!-- These get promoted to root MEMORY.md after ship -->
- Persisted `KnownHostsStore` via DataStore + JSON list (see MEMORY #security /
  "Patterns That Worked").
- Revoke-and-re-verify by clearing trust state and re-invoking the existing flow.
