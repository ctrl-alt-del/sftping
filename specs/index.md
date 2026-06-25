---
last_updated: "2026-06-25"
---

# Feature Index

| ID | Feature | Status | Touches | Depends On |
|----|---------|--------|---------|------------|
| 001 | Connect & Browse | 📋 Planned | catalog, manifest, `sftp/`, `security/`, `data/connection/`, `ui/{shell,connection,files}`, `di/` | — |
| 002 | Atomic File Operations | ✅ Done | `sftp/`, `transfer/`, `ui/{files,transfers}`, manifest | 001 |
| 003 | Resumable Transfers | 📋 Planned | `data/transfer/` (Room), `sftp/`, `transfer/`, catalog | 002 |
| 004 | Background Transfer | 📋 Planned | `work/`, manifest (FGS), `ui/transfers/`, catalog | 003 |
| 005 | Resilience Hardening & Release | 📋 Planned | `transfer/`, `work/`, build config | 004 |

Status: 📋 Planned → 🚧 In Progress → ✅ Done → 📦 Archived

> Mockups so far: 001 (connection / host-key / browser / multi-select) and the
> 002 upload flow, under each feature's `ux-ui/`.
