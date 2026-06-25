# Resilience Hardening & Release — Takeaways

## What Went Well
- R8 keep-rules for JSch + Room entities are minimal and targeted; the mwiede JSch fork uses
  reflection for cipher/key-exchange negotiation, so a blanket `-keep class com.jcraft.jsch.**`
  + `-dontwarn` is sufficient.
- `SecurityException` handling for stale SAF Uris distinguishes between "permission expired"
  (user-resolvable) and generic I/O errors (likely corrupt file), giving the user actionable
  messaging.

## What We Learned
- The applicationId placeholder `com.example.sftping` is not a blocker during development as
  long as it's changed before Play Store submission (Google rejects `com.example.*`).
- Release optimization (`optimization { enable = true }`) with R8 keep-rules requires
  verifying the minified APK works on-device — JSch reflection failures at runtime would
  cause silent cipher negotiation crashes. The keep-rules here should prevent that.
- The `dataSync` foreground service 6h/day cap on Android 15+ is unlikely to be hit during
  typical LAN SFTP transfers but is worth documenting in release notes.

## Patterns Worth Reusing
- **SAF permission expiry**: catch `SecurityException` separately from generic `IOException`
  in ContentResolver operations. The former requires the user to re-pick the file; the latter
  is likely transient or file corruption.

## Remaining for production release
- [ ] Change `applicationId` from `com.example.sftping` to a real identifier
- [ ] Build and test the release APK with R8 enabled (`isMinifyEnabled = true`)
- [ ] Verify SHA-256 fingerprints are displayed in the host-key dialog end-to-end
- [ ] Verify encrypted credentials survive app restart (Keystore + DataStore round-trip)
- [ ] License attribution: JSch BSD-3-Clause, AndroidX Apache-2.0 — OK for closed-source
