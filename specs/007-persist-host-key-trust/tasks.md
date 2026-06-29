# Persist Host-Key Trust & Revoke — Tasks

## Block 0: Spec & Design (before code)
- [x] `doc-coauthoring`: spec.md + plan.md approved
- [x] Mockup: none needed (simple list dialog)
- [x] `test_plan.md`: test scenarios documented

## Block 1: Persistence model & store

- [ ] **Task 1.1**: `TrustedHost` data class + `org.json` helpers — `security/TrustedHost.kt`, `test/security/TrustedHostTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

- [ ] **Task 1.2**: Suspend `KnownHostsStore` (+`remove`/`all`) + `DataStoreKnownHostsStore`; rebind in `SecurityModule`; rewrite store test — `security/KnownHostsStore.kt`, `di/SecurityModule.kt`, `test/security/KnownHostsStoreTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 2: Wire-up

- [ ] **Task 2.1**: `JschSftpClient` awaits suspend store + persists `keyType` — `sftp/JschSftpClient.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

- [ ] **Task 2.2**: `ConnectionViewModel` inject store + list/revoke/revokeAndReverify; fix + extend VM test — `ui/connection/ConnectionViewModel.kt`, `test/ui/connection/ConnectionViewModelTest.kt`
  - Build: `./gradlew assembleDebug`
  - Tests: `./gradlew testDebug`

## Block 3: UI

- [ ] **Task 3.1**: Trusted-hosts manager dialog + Changed-dialog "Revoke & re-verify" — `ui/connection/ConnectionScreen.kt`
  - Build: `./gradlew assembleDebug`
  - Lint: `./gradlew lint`

## Block 4: Docs
- [ ] **Task 4.1**: Update `AGENTS.md`, `README.md`, `MEMORY.md`, `specs/index.md`, `takeaways.md`
