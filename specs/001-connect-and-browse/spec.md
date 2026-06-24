# Connect & Browse — Specification

Covers PRD features **F1** (configure/save connections) and **F2** (browse remote
filesystem), plus the cross-cutting **security baseline** (host-key TOFU,
encrypted credentials) that the PRD lists under NFRs §7.1.

## User Stories

- **F1** — As an operator, I want to enter and save SFTP connection parameters
  (host/port/user, password or key) so I can reconnect to my servers quickly.
- **F2** — As an operator, I want to browse the remote filesystem (enter/exit
  directories, see size/mtime/type) so I can locate files to manage.
- **Security** — As a security-conscious user, I want first-connection host-key
  confirmation and encrypted credential storage so I am protected from MITM and
  local credential theft.

## UX/UI

Mockups: `ux-ui/mockups.png` (source `ux-ui/mockups.html`). Frames:
1. Connection form — host/port/user, password/key segmented toggle, recent
   connections dropdown, "Save credentials" (encrypted) switch, Connect.
2. Host-key verification dialog — SHA-256 fingerprint + key type, Reject / Trust.
3. File browser — breadcrumb, list rows (type icon, name, size, mtime), back/refresh.
4. (Multi-select shown for forward context; selection actions belong to 002.)

Material 3, baseline purple palette; dynamic color on device (Android 12+).

## Acceptance Criteria

### Happy Path
- [ ] Given valid host/port/user/password, when I tap Connect on a **known** host,
      then I land on the file browser showing the remote home directory.
- [ ] Given a **first-time** host, when I tap Connect, then a dialog shows the
      server's SHA-256 fingerprint; on Trust the key is persisted and connection proceeds.
- [ ] Given "Save credentials" is on, when I reconnect later, then the host appears
      in Recent and credentials are retrieved decrypted (never shown in logs).
- [ ] Given a directory, when I tap a folder row I enter it; back returns to parent.
- [ ] File rows display human-readable size (e.g. `1.4 GB`), modified time, and a type icon.

### Edge Cases
- [ ] Wrong credentials → inline auth error, no crash, stay on connection form.
- [ ] Host unreachable / timeout → Snackbar with **Retry**.
- [ ] Host-key **mismatch** vs stored key → blocking warning (possible MITM); refuse to auto-connect.
- [ ] Empty directory → empty-state message, not a blank screen.
- [ ] Permission-denied directory → row-level/Snackbar error, browsing continues elsewhere.
- [ ] Connection drops mid-listing → error surfaced, Retry re-issues the list.

## Non-Functional Requirements

- **Concurrency**: all SFTP I/O off the main thread (`Dispatchers.IO`); UI never blocks.
- **Security**: passwords/keys encrypted via Android Keystore (AES-GCM); host keys
  stored TOFU; fingerprints shown as SHA-256 (not MD5).
- **Performance**: directory listing renders incrementally via `LazyColumn`;
  large directories (1000+ entries) scroll smoothly.
- **Accessibility**: all icon-only controls have content descriptions; touch targets ≥ 48dp.
- **Out of scope** (later features): transfers/upload/download (002), resume (003),
  background/notifications (004).
