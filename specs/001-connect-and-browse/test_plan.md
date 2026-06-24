# Connect & Browse — Test Plan

## Unit Tests (JVM — `app/src/test/`)

### Fingerprint
- [ ] **Happy path**: Arrange a known public-key byte array; Act compute SHA-256
      base64 fingerprint; Assert it equals the expected `SHA256:…` string.
- [ ] **Stability**: same key bytes always produce the same fingerprint.

### KnownHostsStore (logic, store mocked)
- [ ] **First connect**: unknown host → returns `Unknown` (prompt user).
- [ ] **Match**: stored fingerprint equals presented → returns `Trusted`.
- [ ] **Mismatch**: stored ≠ presented → returns `Changed` (MITM warning).

### File formatting / RemoteFile
- [ ] **Size formatter**: 0, 1023, 1024, 1.4e9, -1 (unknown) → "0 B", "1023 B",
      "1.0 KB", "1.4 GB", "—".
- [ ] **Mtime formatter**: epoch seconds → local-timezone display string.
- [ ] **Sort**: directories before files; case-insensitive name order.

### ConnectionProfile / Repository
- [ ] **Round-trip**: profile written to (fake) DataStore reads back equal.
- [ ] **Recents ordering**: most-recently-connected first; capped at N.
- [ ] **No secret leak**: `toString()` / logging path excludes password & key.

## Integration / Instrumented (`app/src/androidTest/`)

### KeystoreCrypto
- [ ] **Round-trip**: encrypt(password) then decrypt → original plaintext.
- [ ] **Tamper**: modified ciphertext → decryption throws, no plaintext leak.

### Connect → Browse (against a local test SFTP server or fake ISftpClient)
- [ ] **Full flow**: enter creds → first-time fingerprint dialog → Trust →
      file list of home dir appears.
- [ ] **Navigate**: tap folder → enters; back → parent; breadcrumb updates.

## Edge Cases
- [ ] Wrong password → auth error shown, form retained.
- [ ] Unreachable host → Snackbar with Retry; Retry re-attempts.
- [ ] Host-key changed → blocking warning, no auto-connect.
- [ ] Empty directory → empty-state message.
- [ ] Permission-denied directory → error surfaced, app stable.
- [ ] Mid-listing disconnect → error + last listing preserved.
