# Files Page: Remember Last Visited Path — Specification

## User Story
As a user browsing remote files, I want the Files tab to reopen the **last
directory I was viewing** when I switch back to it from another tab (Connect /
Transfers), so that I don't lose my place every time I check a transfer.

## UX/UI
- [ ] Mockup: none — no visible UI change; behavior only.
- Switching away from Files and back reopens the last visited directory (and its
  back-stack), re-listed for freshness.
- Establishing a **new connection** still opens the host's home/default directory
  (feature 008 behavior).

## Acceptance Criteria

### Happy Path
- [ ] Given I navigated into `/var/www/logs`, when I switch to Transfers and back
  to Files, then Files reopens `/var/www/logs` (not the home directory).
- [ ] Given I return to the Files tab in the same session, then the directory is
  re-listed (fresh) and the back-stack is preserved.
- [ ] Given I connect (or reconnect), when I open Files, then it opens the host's
  home/default directory and the back-stack/search are reset.

### Edge Cases
- [ ] Reconnecting to a different host does not reopen a stale path from the
  previous session — it resets to the new home/default directory.
- [ ] The first time Files is opened after connecting, it loads the home/default
  directory (no remembered path yet).

## Non-Functional Requirements
- State is in-memory for the lifetime of the connection/session (a live SFTP
  session does not survive process death, so no persistence is required).
- Performance: returning to the tab costs one directory listing (same as today).
