# Resumable Transfers — Specification

Covers PRD feature **F4** (resume after pause / network drop) and the
persistence + large-file strategy from PRD §4.2, §5.1, §5.2.

## User Stories
- As an operator transferring a >1 GB file, I want it to **resume** from where it
  stopped after I pause it or the network drops, so I don't restart from zero.

## UX/UI
- [ ] Transfer card with pause/resume; resume picks up at the saved offset.
- [ ] Mockups deferred until this feature is authored in detail.

## Acceptance Criteria

### Happy Path
- [ ] Pause a download → `transferredBytes` persisted → resume continues from that
      offset → final file byte-identical to source (size + checksum).
- [ ] Network drop mid-transfer → task enters a recoverable state → resume succeeds.
- [ ] Upload resume when the server supports it; otherwise overwrite-from-start.

### Edge Cases
- [ ] App killed mid-transfer → task state recovered from Room on next launch.
- [ ] Remote file changed since partial transfer → detect (size/mtime) and restart.
- [ ] Server lacks upload-resume → graceful fallback, user informed.

## Non-Functional Requirements
- 1 MB buffer; `serverAliveInterval` to survive NAT timeouts; compression off.
- Progress persisted at most every ~1 MB to bound DB writes.
- **Out of scope**: background execution + notifications + queue UI (004).
