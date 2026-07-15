# Session Health Monitoring — Tasks

## Block 0: Spec
- [x] `doc-coauthoring`: spec.md + plan.md
- [x] Mockup: skipped (no UI change)
- [x] `test_plan.md`: test scenarios documented

## Block 1: Fixes
- [x] **Task 1.1**: Add `@Volatile` to `JschSftpClient.session` field — `sftp/JschSftpClient.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`
- [x] **Task 1.2**: Detect dead session in `openChannel()`; `disconnectInternal()` if
      session is non-null but `isConnected == false` — `sftp/JschSftpClient.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: Docs
- [x] **Task 2.1**: takeaways.md; promote to MEMORY.md; update specs/index.md;
      flip plan.md status → ✅ Done
