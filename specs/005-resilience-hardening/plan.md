---
feature_id: "005"
name: "Resilience Hardening & Release"
status: "✅ Done"
depends_on: ["004"]
touches:
  - "app/src/main/java/com/example/sftping/transfer/"
  - "app/src/main/java/com/example/sftping/work/"
  - "app/build.gradle.kts"
created: "2026-06-25"
---

# Resilience Hardening & Release — Plan

> Seed spec from PRD §8 Phase 5 + §7 NFRs. Tasks detailed when started.

## Approach
Harden the transfer pipeline against extreme conditions and prepare for release.

- **Fault injection**: airplane-mode toggle, disk-full, server restart mid-transfer.
- **Data integrity**: verify no corruption after recovery (checksum/size).
- **Security verification**: end-to-end check of host-key TOFU + encrypted
  credentials (built in 001); confirm SHA-256 fingerprints, no secrets in logs.
- **Release checklist**:
  - ⚡ Change `applicationId` from the placeholder `com.example.sftping` to a real
    id (Play Store rejects `com.example.*`). See MEMORY ADR-005.
  - License compliance: JSch (BSD-3-Clause), AndroidX (Apache-2.0) — OK for closed-source.
  - ProGuard/R8 rules for JSch reflection if minification is enabled.

## Files to Create / Change
Mostly hardening + tests across transfer/work layers; build config for release.

## Risks
- Reproducing extreme scenarios reliably in CI vs. on-device.

## Dependencies
Requires 004 (full transfer pipeline). Final feature.
