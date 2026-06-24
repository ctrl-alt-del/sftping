# MEMORY — Accumulated Project Knowledge

## 🧠 Tech Gotchas
<!-- Tagged: #api #ui #build #security — AI searches by tag -->
<!-- ⚡ = broke in production / non-negotiable guardrail -->

- ⚡ `#api` Use the **mwiede JSch fork** (`com.github.mwiede:jsch`, ≥ 0.2.15), never
  `com.jcraft:jsch` (abandoned at 0.1.55, vulnerable to Terrapin / CVE-2023-48795).
- `#api` JSch resume signatures: download is
  `ChannelSftp.get(src, OutputStream, monitor, ChannelSftp.RESUME, skip)`; upload is
  `put(InputStream, dst, monitor, ChannelSftp.RESUME)`. The PRD's
  `get(remote, out, localSize, monitor)` argument order is **wrong** — go through
  the `ISftpClient` abstraction and map carefully.
- ⚡ `#ui` SAF `ContentResolver.openOutputStream` cannot be arbitrarily seeked, and
  append mode (`"wa"`) support is provider-dependent → resumable **downloads** must
  write to an app-private (seekable) cache file, then copy to the SAF destination on
  completion.
- `#build` WorkManager workers have a ~10-minute execution window, and `dataSync`
  foreground services have a ~6h/day cap on Android 15+ → implement pause as
  **stop worker + persist byte offset**, resume as **re-enqueue from offset**.
  Never a `while (isPaused) delay()` busy-wait.
- ⚡ `#security` `androidx.security:security-crypto` (EncryptedSharedPreferences) is
  deprecated (last release 1.1.0-alpha06) → encrypt credentials with **Android
  Keystore AES-GCM** (optionally Tink) and store ciphertext in DataStore.
- `#security` Show host-key fingerprints as **SHA-256** (TOFU), not MD5; never use
  `StrictHostKeyChecking=no` in production.
- `#build` Tooling is intentionally bleeding-edge (AGP 9.2.1, Kotlin 2.2.10,
  compileSdk 36, Compose BOM 2025.12.00). Verify Hilt / KSP / Room versions against
  the catalog before trusting web snippets.

## 🔧 Patterns That Worked
<!-- Reusable patterns discovered across features -->

## 📐 Architecture Decisions
<!-- ADRs made during spec-driven development -->
- ADR-001: Specs in specs/ separate from docs/ (permanent reference vs per-feature artifacts)
- ADR-002: SFTP library = JSch (mwiede fork), not SSHJ — lighter, BSD-3-Clause,
  matches PRD, Terrapin-patched.
- ADR-003: Transfer pause = stop worker + persist byte offset; resume = re-enqueue
  from offset (avoids WorkManager / FGS time limits).
- ADR-004: Resumable downloads land in a private cache file, then copy to the SAF
  target (SAF is not reliably seekable).
- ADR-005: DI = Hilt (with `HiltWorker` for WorkManager). ⚡ `applicationId` stays
  `com.example.sftping` until release — **must** change before Play Store submission
  (rejects `com.example.*`).

## 📂 Code Ownership Map

| File | Touched By | Why |
|------|-----------|-----|
| — | — | _(fills in as 001 ships)_ |

## 🐛 Common Bugs Fixed

## 🧠 AI Workflow Rule

Before writing any spec, read in order:
1. `AGENTS.md` or `CLAUDE.md` — project conventions
2. `specs/SDD.md` — SDD workflow
3. `MEMORY.md` — search for relevant #tags
4. `specs/index.md` — check for feature file conflicts

After shipping a feature:
1. Write `takeaways.md` in the feature folder
2. Curate findings into `MEMORY.md` (tagged, ⚡ for critical)
3. Update code ownership map
