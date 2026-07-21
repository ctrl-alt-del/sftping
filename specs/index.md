---
last_updated: "2026-07-17"
---

# Feature Index

| ID | Feature | Status | Touches | Depends On |
|----|---------|--------|---------|------------|
| 001 | Connect & Browse | ✅ Done | catalog, manifest, `sftp/`, `security/`, `data/connection/`, `ui/{shell,connection,files}`, `di/` | — |
| 002 | Atomic File Operations | ✅ Done | `sftp/`, `transfer/`, `ui/{files,transfers}`, manifest | 001 |
| 003 | Resumable Transfers | ✅ Done | `data/transfer/` (Room), `sftp/`, `transfer/`, catalog | 002 |
| 004 | Background Transfer | ✅ Done | `work/`, manifest (FGS), `ui/transfers/`, catalog | 003 |
| 005 | Resilience Hardening & Release | ✅ Done | `transfer/`, `work/`, build config | 004 |
| 006 | TransferManager Refactor | ✅ Done | `transfer/{strategy,usecase}`, `transfer/TransferManager.kt`, `work/`, `sftp/`, `di/` | — |
| 007 | Persist Host-Key Trust & Revoke | ✅ Done | `security/`, `di/`, `sftp/`, `ui/connection/` | 001 |
| 008 | Connect Page: Password Visibility & Default Directory | ✅ Done | `sftp/`, `data/connection/`, `ui/connection/`, `ui/files/` | 001 |
| 009 | Files Page: Hidden Toggle, Sort & Search | ✅ Done | `ui/files/` | 001 |
| 010 | Files Page: Remember Last Visited Path | ✅ Done | `sftp/`, `ui/connection/`, `ui/files/` | 008 |
| 011 | Batch Transfers & Uploaded-File Memory | ✅ Done | `ui/files/`, `transfer/`, catalog, `app/build.gradle.kts` | 002, 008 |
| 012 | Retry Failed Upload | ✅ Done | `transfer/`, `ui/transfers/` | 002 |
| 013 | Grouped Transfers — Collapsible Sections & Batch Retry | ✅ Done | `transfer/TransferManager.kt`, `ui/transfers/` | 012 |
| 014 | Remote File Editor | ✅ Done | `MainActivity.kt`, `sftp/`, `data/editor/`, `di/EditorModule.kt`, `ui/editor/`, `AGENTS.md`, `README.md` | 001 |
| 015 | Files Item Actions (Long-Press Menu) | ✅ Done | `ui/files/`, `ui/editor/`, `sftp/SessionState.kt`, `util/`, `di/ClipboardModule.kt`, `MainActivity.kt` | 009, 014 |
| 016 | Session Health & Stale Connected State | ✅ Done | `sftp/JschSftpClient.kt` | 001, 008, 010, 014 |
| 017 | CI Integration (GitHub Actions) | ✅ Done | `.github/workflows/ci.yml`, `AGENTS.md`, `README.md`, `MEMORY.md` | — |
| 018 | Connection Status Indicator | ✅ Done | `ui/components/`, `ui/files/`, `ui/transfers/`, `ui/editor/` | 001, 008, 016 |

Status: 📋 Planned → 🚧 In Progress → ✅ Done → 📦 Archived

> Mockups so far: 001 (connection / host-key / browser / multi-select) and the
> 002 upload flow, under each feature's `ux-ui/`.
