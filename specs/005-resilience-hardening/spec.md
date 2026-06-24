# Resilience Hardening & Release — Specification

Covers PRD §8 Phase 5 (extreme-scenario testing) and release-readiness from §7
(security, license) and §9 (edge cases).

## User Stories
- As an operator, I want transfers to never corrupt data even when the network,
  disk, or server misbehaves, so I can trust the app with critical files.

## UX/UI
- No new screens; refinements to error/recovery messaging only.

## Acceptance Criteria

### Happy Path
- [ ] Airplane mode ON during transfer → task pauses/waits; OFF → resumes; result intact.
- [ ] Server restarts mid-transfer → reconnect + resume; result byte-identical.
- [ ] Disk full on download → clear error, no corrupt partial promoted to destination.

### Edge Cases
- [ ] SAF Uri invalidated after reboot → re-authorization prompt (handle `SecurityException`).
- [ ] Server time zone vs. local → mtimes displayed in local time.
- [ ] Host-key changed between sessions → MITM warning (verify 001 behavior end-to-end).

## Non-Functional Requirements
- **Security**: confirm credentials encrypted at rest; no secrets logged; SHA-256 fingerprints.
- **Licensing**: JSch BSD-3-Clause, AndroidX Apache-2.0 — verified for distribution.
- **Release**: real `applicationId` set; R8 keep-rules for JSch validated.
