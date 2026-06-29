# Persist Host-Key Trust & Revoke — Specification

## User Story
As a user who trusts a server's host key, I want that trust to **survive app
restarts** and to be able to **revoke** a trusted key, so that I am not asked to
re-verify every fingerprint after process death and I can drop a key I no longer
trust (e.g. after a legitimate server key rotation or a suspected compromise).

## UX/UI
- [ ] Mockup: simple list dialog — no `canvas-design` mockup required.
- **Trusted-hosts manager**: a shield/list action in the Connect screen
  `TopAppBar` opens an `AlertDialog` listing every trusted host
  (`host` · key type · trusted date) with a per-row **Revoke** action. Empty
  state shows "No trusted hosts yet."
- **Changed-key dialog**: the existing "Host key changed!" dialog gains a
  **"Revoke & re-verify"** action that removes the stored key and re-runs the
  connection, surfacing the new key as `Unknown` for fresh verification.

## Acceptance Criteria

### Happy Path
- [ ] Given a host trusted in a previous run, when the app is killed and the user
  reconnects, then `connect()` returns `Trusted` without prompting (trust is read
  from DataStore).
- [ ] Given a trusted host, when the user opens the trusted-hosts manager and taps
  Revoke, then the entry is removed and the next connection returns `Unknown`.
- [ ] Given a `Changed` result, when the user taps "Revoke & re-verify", then the
  stored key is removed and the connection re-runs, returning `Unknown` for the
  newly presented key.
- [ ] When the user taps "Trust & connect" on an `Unknown` key, then a
  `TrustedHost` (host, SHA-256 fingerprint, key type, timestamp) is persisted.

### Edge Cases
- [ ] When the stored DataStore JSON is empty or malformed, then `all()` returns an
  empty list and the app treats every host as `Unknown` (no crash).
- [ ] When revoking a host that is not stored, then the operation is a no-op.
- [ ] When two endpoints share a hostname, they share one entry (keyed by host —
  see ADR-006).

## Non-Functional Requirements
- Security: host fingerprints are **public keys, not secrets** → stored as
  plaintext JSON in DataStore (no Android Keystore). Credentials remain encrypted
  in `SecretStore`. TOFU is SHA-256; never `StrictHostKeyChecking=no` semantics in
  the app layer.
- Offline: fully offline (local DataStore only).
- Performance: known-hosts set is tiny; a single JSON-blob preference key is
  sufficient (mirrors `ConnectionRepository`).
