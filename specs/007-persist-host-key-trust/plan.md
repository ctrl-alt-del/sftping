---
feature_id: "007"
name: "Persist Host-Key Trust & Revoke"
status: "✅ Done"
depends_on: ["001"]
touches:
  - "app/src/main/java/com/example/sftping/security/TrustedHost.kt"
  - "app/src/main/java/com/example/sftping/security/KnownHostsStore.kt"
  - "app/src/main/java/com/example/sftping/di/SecurityModule.kt"
  - "app/src/main/java/com/example/sftping/sftp/JschSftpClient.kt"
  - "app/src/main/java/com/example/sftping/ui/connection/ConnectionViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/connection/ConnectionScreen.kt"
  - "app/src/test/java/com/example/sftping/security/TrustedHostTest.kt"
  - "app/src/test/java/com/example/sftping/security/KnownHostsStoreTest.kt"
  - "app/src/test/java/com/example/sftping/ui/connection/ConnectionViewModelTest.kt"
created: "2026-06-29"
---

# Persist Host-Key Trust & Revoke — Plan

## Approach

Replace the in-memory `InMemoryKnownHostsStore` with a DataStore-backed
`DataStoreKnownHostsStore`, mirroring the existing `ConnectionRepository` pattern
(DataStore Preferences + a single JSON-blob key). Trust records are modeled as a
new pure `TrustedHost` data class with `org.json` serialization helpers (testable
on the JVM, exactly like `ConnectionProfile`).

The `KnownHostsStore` interface becomes `suspend` and gains `remove(host)` and
`all()`. All current callers (`JschSftpClient.connect` / `trustAndProceed`) already
run inside `withContext(Dispatchers.IO)`, so the suspend change is a clean swap;
`put` additionally records the key type.

Revocation is exposed two ways: a trusted-hosts manager dialog opened from the
Connect screen, and a "Revoke & re-verify" action on the existing "Host key
changed!" dialog (removes the stored key, then re-runs `connect()` which now
returns `Unknown`). `ConnectionViewModel` gains an injected `KnownHostsStore`.

No `libs.versions.toml` change is required (DataStore + `org.json` already present).
No migration is required — the previous store persisted nothing, so the first run
after upgrade behaves exactly as before (re-verify once).

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `security/TrustedHost.kt` | Pure model + JSON helpers (host, fingerprint, keyType, trustedAt) |
| Edit | `security/KnownHostsStore.kt` | suspend `get`/`put`/`remove`/`all`; new `DataStoreKnownHostsStore` |
| Edit | `di/SecurityModule.kt` | Bind `DataStoreKnownHostsStore` |
| Edit | `sftp/JschSftpClient.kt` | Await suspend store; persist `hostKey.type` |
| Edit | `ui/connection/ConnectionViewModel.kt` | Inject store; list/revoke/revokeAndReverify state + actions |
| Edit | `ui/connection/ConnectionScreen.kt` | Trusted-hosts manager dialog + Changed-dialog revoke |
| Create | `test/security/TrustedHostTest.kt` | JSON round-trip + edge cases |
| Edit | `test/security/KnownHostsStoreTest.kt` | Rewrite for suspend + remove/all |
| Edit | `test/ui/connection/ConnectionViewModelTest.kt` | New store mock + revoke/re-verify tests |

## Risks
- Suspend interface ripple touches the store, DI binding, JSch client, VM, and 3
  test files — covered by the task ordering (interface + impl land together).
- `DataStoreKnownHostsStore` is not directly JVM-unit-testable (DataStore needs a
  device); pure logic is isolated in `TrustedHost` and tested there (same posture
  as `ConnectionRepository`/`ConnectionProfile`).

## Dependencies
- 001 (Connect & Browse) — owns `KnownHostsStore`, `JschSftpClient`, the Connect UI.

## ADR-006
Persist trusted host keys in DataStore as **plaintext JSON** (fingerprints are
public keys, not secrets — no Keystore). Key entries by **host** (preserves
current behavior; SSH-endpoint/port scoping deferred). Revoke = remove the entry;
the Changed dialog's "Revoke & re-verify" re-runs `connect()` to re-trust the new
key as `Unknown`.
