---
feature_id: "002"
name: "Atomic File Operations"
status: "✅ Done"
depends_on: ["001"]
touches:
  - "app/src/main/java/com/example/sftping/sftp/ISftpClient.kt"
  - "app/src/main/java/com/example/sftping/sftp/JschSftpClient.kt"
  - "app/src/main/java/com/example/sftping/transfer/"
  - "app/src/main/java/com/example/sftping/ui/files/"
  - "app/src/main/java/com/example/sftping/ui/transfers/"
  - "app/src/main/AndroidManifest.xml"
created: "2026-06-25"
---

# Atomic File Operations — Plan

> Seed spec from PRD F3. Tasks detailed when this feature is started.

## Approach

Add single-operation transfers and management on top of the 001 SFTP session,
**without** resume or background execution (those are 003 / 004). Transfers run
while the app is foregrounded; cancelling aborts the stream.

- **Download/Upload**: stream via SAF. Per the SDD decision, download writes to an
  app-private cache file first, then copies to the user-picked SAF destination on
  completion (avoids unreliable SAF seek/append). Upload reads the SAF `InputStream`.
- **Delete/Rename**: direct `ChannelSftp.rm` / `rename`.
- **Transfers UI**: a basic active/completed list (full queue + ETA is 004).

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Change | `sftp/ISftpClient.kt`, `JschSftpClient.kt` | Implement download/upload/delete/rename |
| Create | `transfer/*` | In-memory transfer manager + progress Flow (no Room yet) |
| Change | `ui/files/*` | FAB upload entry, multi-select bulk delete, rename dialog |
| Create | `ui/transfers/*` | Active/completed transfer list |

## Risks
- SAF `InputStream`/`OutputStream` lifecycle and `takePersistableUriPermission`.
- JSch `put`/`get` exception mapping; partial-write cleanup on cancel.

## Dependencies
Requires 001 (session, ISftpClient, file browser). Unblocks 003.
