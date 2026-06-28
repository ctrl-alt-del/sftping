---
last_updated: "2026-06-29"
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

Status: 📋 Planned → 🚧 In Progress → ✅ Done → 📦 Archived

> Mockups so far: 001 (connection / host-key / browser / multi-select) and the
> 002 upload flow, under each feature's `ux-ui/`.
