---
feature_id: "001"
name: "Connect & Browse"
status: "📋 Planned"
depends_on: []
touches:
  - "gradle/libs.versions.toml"
  - "app/build.gradle.kts"
  - "app/src/main/AndroidManifest.xml"
  - "app/src/main/java/com/example/sftping/SftpingApp.kt"
  - "app/src/main/java/com/example/sftping/MainActivity.kt"
  - "app/src/main/java/com/example/sftping/ui/shell/"
  - "app/src/main/java/com/example/sftping/ui/connection/"
  - "app/src/main/java/com/example/sftping/ui/files/"
  - "app/src/main/java/com/example/sftping/sftp/"
  - "app/src/main/java/com/example/sftping/data/connection/"
  - "app/src/main/java/com/example/sftping/security/"
  - "app/src/main/java/com/example/sftping/di/"
created: "2026-06-25"
---

# Connect & Browse — Plan

## Approach

Establish the app's architecture skeleton and deliver the first usable slice: a
user can save a connection, connect over SFTP (with first-time host-key
confirmation), and browse the remote filesystem.

Layered per `specs/SDD.md` Tech Stack section:

```
UI (Compose + ViewModel/StateFlow)
  → Repository (ConnectionRepository, SftpRepository)
    → Data source (JschSftpClient, DataStore, Keystore crypto)
```

- **DI**: Hilt (`@HiltAndroidApp` Application, `@HiltViewModel`, `@Module` providers).
  Chosen for clean repository wiring and first-class `HiltWorker` support later (004).
- **Concurrency**: JSch is blocking → all `ISftpClient` calls run on
  `Dispatchers.IO`; results surface as suspend functions / `Flow`.
- **Security baseline lands here, not later**: host-key TOFU + encrypted
  credentials are foundational and cheap to do up front (see MEMORY `#security`).

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Change | `gradle/libs.versions.toml` | Add jsch(mwiede), hilt, ksp, datastore, coroutines, lifecycle-viewmodel-compose |
| Change | `app/build.gradle.kts` | Apply hilt + ksp plugins; wire deps from catalog |
| Change | `app/src/main/AndroidManifest.xml` | INTERNET, ACCESS_NETWORK_STATE; register Application |
| Create | `SftpingApp.kt` | `@HiltAndroidApp` Application |
| Change | `MainActivity.kt` | Replace template nav with app shell |
| Create | `ui/shell/*` | Bottom nav scaffold: Connect / Files / Transfers |
| Create | `sftp/RemoteFile.kt` | Domain model for a remote entry |
| Create | `sftp/ISftpClient.kt` | Contract (connect/listFiles/disconnect; resume stubs declared for 002/003) |
| Create | `sftp/JschSftpClient.kt` | mwiede JSch impl on `Dispatchers.IO` |
| Create | `sftp/hostkey/*` | `KnownHostsStore` (TOFU) + SHA-256 fingerprint util |
| Create | `data/connection/ConnectionProfile.kt`, `ConnectionRepository.kt` | DataStore-backed non-secret config + recents |
| Create | `security/SecretStore.kt`, `KeystoreCrypto.kt` | AES-GCM via Android Keystore for passwords/keys |
| Create | `ui/connection/*` | ConnectionScreen + ConnectionViewModel |
| Create | `ui/files/*` | FilesScreen + FilesViewModel |
| Create | `di/*` | Hilt modules |

## Risks

- **JSch flavor**: must use `com.github.mwiede:jsch`, NOT `com.jcraft:jsch`
  (abandoned, CVE-vulnerable). See MEMORY `#api ⚡`.
- **NetworkOnMainThread**: any accidental main-thread SFTP call crashes — enforce IO dispatcher.
- **Keystore on JVM tests**: AES-GCM crypto needs a device/emulator; cover via
  instrumented test, keep formatting/logic in pure classes for JVM unit tests.
- **Bleeding-edge tooling** (AGP 9.x / Kotlin 2.2.x / compileSdk 36): verify Hilt
  + KSP versions are compatible with the catalog before assuming web snippets work.

## Dependencies

None (root feature). Unblocks 002.
