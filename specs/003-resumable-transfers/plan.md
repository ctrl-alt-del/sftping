---
feature_id: "003"
name: "Resumable Transfers"
status: "📋 Planned"
depends_on: ["002"]
touches:
  - "app/src/main/java/com/example/sftping/sftp/JschSftpClient.kt"
  - "app/src/main/java/com/example/sftping/data/transfer/"
  - "app/src/main/java/com/example/sftping/transfer/"
  - "gradle/libs.versions.toml"
  - "app/build.gradle.kts"
created: "2026-06-25"
---

# Resumable Transfers — Plan

> Seed spec from PRD F4 + §4.2 + §5.1/§5.2. Tasks detailed when started.

## Approach
Persist transfer state in Room so transfers survive pause and network drops, and
resume from the last byte offset.

- **Room**: `transfer_tasks` table (PRD §4.2) — id, remotePath, localUri,
  fileName, totalSize, transferredBytes, direction, status, lastModified, speed.
- **Resume (download)**: JSch `get(src, OutputStream, monitor, ChannelSftp.RESUME, skip)`;
  write to the app-private cache file (seekable), update `transferredBytes` every ~1 MB.
- **Resume (upload)**: `stat` remote size → `put(InputStream, dst, monitor, ChannelSftp.RESUME)`;
  if the server lacks resume support, fall back to overwrite-from-start.
- **Large files (>1 GB)**: 1 MB buffer, `setServerAliveInterval(30s)`,
  compression off, streamed byte loop (never `readAllBytes`).
- **Pause model** (per SDD): pause = stop work + persist offset; resume = continue
  from `transferredBytes` (not a busy-wait loop).

> See MEMORY `#api` for the exact JSch resume signatures and the jcraft-vs-mwiede note.

## Files to Create / Change
| Action | File | Rationale |
|--------|------|-----------|
| Change | `gradle/libs.versions.toml`, `app/build.gradle.kts` | Add Room + KSP for Room |
| Create | `data/transfer/*` | Room `TransferTask` entity, DAO, database |
| Change | `sftp/JschSftpClient.kt` | Implement resumable get/put with skip + monitor |
| Change | `transfer/*` | Persist progress, resume orchestration |

## Risks
- JSch resume overload misuse (arg order); server without upload-resume.
- Offset accuracy across pause/resume (verify byte-exactness).

## Dependencies
Requires 002. Unblocks 004.
