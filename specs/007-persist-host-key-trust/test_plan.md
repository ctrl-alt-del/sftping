# Persist Host-Key Trust & Revoke — Test Plan

## Unit Tests

### TrustedHost (JVM)
- [ ] **Happy path**: Arrange a `TrustedHost`; Act `listToJson` then `listFromJson`;
  Assert the round-tripped list equals the original (all fields).
- [ ] **Empty list**: Arrange `emptyList()`; Act round-trip; Assert empty list.
- [ ] **Missing fields**: Arrange JSON object lacking `keyType`/`trustedAt`; Act
  `fromJson`; Assert defaults (`""` / `0L`) applied, no exception.
- [ ] **Malformed JSON**: Arrange `"not json"`; Act `listFromJson`; Assert it throws
  (caller — the store — guards with try/catch and returns empty).

### KnownHostsStore (JVM, in-memory test double or DataStore-free logic)
- [ ] **put/get**: Arrange empty store; Act `put` then `get`; Assert fingerprint.
- [ ] **overwrite**: Act `put` twice same host; Assert latest fingerprint.
- [ ] **remove**: Arrange a stored host; Act `remove`; Assert `get` is null and it
  is absent from `all()`.
- [ ] **all**: Arrange two hosts; Act `all()`; Assert both returned with metadata.

### ConnectionViewModel (JVM)
- [ ] **loadTrustedHosts**: Arrange store `all()` returns two; Act init/load; Assert
  `uiState.trustedHosts` size 2.
- [ ] **revokeTrustedHost**: Act `revokeTrustedHost("h")`; Assert `store.remove("h")`
  verified and list refreshed.
- [ ] **revokeAndReverify**: Arrange `Changed` result + `connect` returns `Unknown`;
  Act `revokeAndReverify()`; Assert `store.remove` called and `hostKeyResult` is
  `Unknown`.
- [ ] **existing tests still pass**: all 5 prior VM constructions updated with the
  new `KnownHostsStore` mock.

## Integration / UI
- [ ] **Full flow** (manual/instrumented): trust a host → kill app → reconnect →
  `Trusted` (no prompt). Open manager → Revoke → reconnect → `Unknown`.

## Edge Cases
- [ ] Revoke a non-existent host → no-op, no crash.
- [ ] Corrupted/empty `known_hosts` DataStore → treated as no trusted hosts.
- [ ] Two ports on one host share an entry (keyed by host, ADR-006).
