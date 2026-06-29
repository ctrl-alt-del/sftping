# Connect Page: Password Visibility & Default Directory — Specification

## User Story
As a user filling in the connection form, I want to (1) reveal my password to
check it, and (2) optionally set a default starting directory (defaulting to the
host's home directory), so that I can avoid typos and land directly in the folder
I care about after connecting.

## UX/UI
- [ ] Mockup: minor additive changes to the existing connect form — no new mockup.
- **Password field**: an eye `trailingIcon` toggles masking on/off. Shown only in
  password mode (when "use key auth" is off, the field already shows plaintext).
- **Default directory field**: a new optional `OutlinedTextField` labelled
  "Default directory (optional)" with placeholder hinting it defaults to the home
  directory. Persisted with recent connections and pre-filled on recall.

## Acceptance Criteria

### Happy Path
- [ ] Given the password field is masked, when the user taps the eye icon, then the
  password is shown in plaintext; tapping again re-masks it.
- [ ] Given the default directory is left blank, when the user connects, then the
  Files screen opens at the host's home directory (resolved via SFTP).
- [ ] Given the user enters `/var/www`, when the user connects, then the Files
  screen opens at `/var/www`.
- [ ] Given a saved recent connection with a default directory, when the user
  selects it, then the field is pre-filled and used on the next connect.

### Edge Cases
- [ ] When the home directory cannot be resolved (blank/error), then the start
  directory falls back to `/`.
- [ ] When the chosen directory cannot be listed (missing / no permission), then
  the Files screen shows the normal "List failed" error and stays (no auto-fallback).
- [ ] In key-auth mode, the eye toggle is not shown (the path is already plaintext).

## Non-Functional Requirements
- Security: revealing the password is an explicit, transient UI action; nothing is
  logged. The default directory is non-secret and stored in the (public) profile.
- Offline: the directory field and toggle work without network; home resolution
  happens only on connect.
- Performance: home resolution is one SFTP round-trip at connect time.
