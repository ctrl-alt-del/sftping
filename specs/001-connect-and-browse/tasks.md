# Connect & Browse — Tasks

One task = one commit. Each task must leave the build green
(`./gradlew assembleDebug`) and tests passing (`./gradlew testDebug`).

## Block 0: Spec & Design (before code)
- [x] `canvas-design`/`theme-factory`: mockups generated → `ux-ui/mockups.png`
- [ ] `doc-coauthoring`: spec.md + plan.md approved by stakeholder
- [ ] `test_plan.md`: scenarios documented

## Block 1: Project scaffolding
- [ ] **T1.1**: Add dependencies to version catalog + apply plugins — `gradle/libs.versions.toml`, `app/build.gradle.kts`
  - jsch (`com.github.mwiede:jsch`), hilt + hilt-compiler, ksp plugin, datastore-preferences, kotlinx-coroutines, lifecycle-viewmodel-compose
  - Build: `./gradlew assembleDebug`
- [ ] **T1.2**: `@HiltAndroidApp` Application + manifest permissions — `SftpingApp.kt`, `AndroidManifest.xml`
  - INTERNET, ACCESS_NETWORK_STATE; register `android:name=".SftpingApp"`
  - Build: `./gradlew assembleDebug`
- [ ] **T1.3**: Replace template nav with app shell; remove `Greeting` + example tests — `MainActivity.kt`, `ui/shell/*`
  - Bottom nav: Connect / Files / Transfers (placeholders for now)
  - Build + Tests: `./gradlew assembleDebug testDebug`

## Block 2: SFTP core
- [ ] **T2.1**: `RemoteFile` model + `ISftpClient` contract — `sftp/RemoteFile.kt`, `sftp/ISftpClient.kt`
  - Declare connect/listFiles/disconnect; declare (not implement) resume methods for 002/003
  - Tests: model + size/mtime formatter unit tests
- [ ] **T2.2**: `JschSftpClient` connect/listFiles/disconnect on `Dispatchers.IO` — `sftp/JschSftpClient.kt`
  - `serverAliveInterval`, no compression default; map JSch exceptions → domain errors
  - Build + Tests
- [ ] **T2.3**: Host-key TOFU — `sftp/hostkey/KnownHostsStore.kt`, `Fingerprint.kt`
  - SHA-256 base64 fingerprint formatting; store/compare keys; mismatch detection
  - Tests: fingerprint formatting + mismatch logic (pure)

## Block 3: Persistence & security
- [ ] **T3.1**: `ConnectionProfile` + DataStore repo (non-secret fields, recents) — `data/connection/*`
  - Tests: serialization + recents ordering
- [ ] **T3.2**: Keystore AES-GCM credential crypto — `security/KeystoreCrypto.kt`, `SecretStore.kt`
  - Instrumented test (Keystore requires device): round-trip encrypt/decrypt

## Block 4: UI
- [ ] **T4.1**: Connection screen + `ConnectionViewModel` (StateFlow) — `ui/connection/*`
  - Fields, password/key toggle, recents dropdown, save-credentials switch
- [ ] **T4.2**: Host-key confirmation dialog wired into connect flow — `ui/connection/*`
- [ ] **T4.3**: File browser + `FilesViewModel` — `ui/files/*`
  - listFiles, enter/back, type icons, size/mtime formatting, empty state
- [ ] **T4.4**: Navigation Connection→Files + error Snackbar with Retry — `ui/shell/*`
  - Build + Tests
