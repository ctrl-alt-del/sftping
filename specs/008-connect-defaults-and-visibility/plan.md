---
feature_id: "008"
name: "Connect Page: Password Visibility & Default Directory"
status: "✅ Done"
depends_on: ["001"]
touches:
  - "app/src/main/java/com/example/sftping/sftp/SessionState.kt"
  - "app/src/main/java/com/example/sftping/sftp/ISftpClient.kt"
  - "app/src/main/java/com/example/sftping/sftp/JschSftpClient.kt"
  - "app/src/main/java/com/example/sftping/data/connection/ConnectionProfile.kt"
  - "app/src/main/java/com/example/sftping/ui/connection/ConnectionViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/connection/ConnectionScreen.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesViewModel.kt"
  - "app/src/test/java/com/example/sftping/data/connection/ConnectionProfileTest.kt"
  - "app/src/test/java/com/example/sftping/ui/connection/ConnectionViewModelTest.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FilesViewModelTest.kt"
created: "2026-06-29"
---

# Connect Page: Password Visibility & Default Directory — Plan

## Approach

Two additive changes to the connect flow.

**Password visibility** is pure UI: a local `passwordVisible` state in
`ConnectionScreen` flips the field's `visualTransformation` and renders an eye
`trailingIcon`. The icon is shown only in password mode (key-auth mode already
shows the path in plaintext). No ViewModel change.

**Default directory** bridges the otherwise-independent `ConnectionViewModel` and
`FilesViewModel` (they share only the `@Singleton ISftpClient`). A new
`@Singleton SessionState` holds the resolved `initialDirectory`. On a successful
connect, `ConnectionViewModel` resolves `enteredDir.ifBlank { homeDirectory() }
?: "/"` and writes it to `SessionState`; `FilesViewModel.loadFiles` defaults to
`sessionState.initialDirectory` so the first/every entry opens there (today it
always resets to `/`, so this is behavior-preserving). The host's home directory
is read via a new `ISftpClient.homeDirectory()` backed by JSch
`ChannelSftp.getHome()`. The directory is persisted on `ConnectionProfile` and
pre-filled when recalling a recent connection.

No catalog/build change is needed (`material-icons-extended` is already a
dependency; `getHome()` is part of the mwiede JSch fork).

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `sftp/SessionState.kt` | `@Singleton` holder bridging Connect → Files start dir |
| Edit | `sftp/ISftpClient.kt`, `sftp/JschSftpClient.kt` | `homeDirectory()` via `getHome()` |
| Edit | `data/connection/ConnectionProfile.kt` | persist `defaultDirectory` |
| Edit | `ui/connection/ConnectionViewModel.kt` | dir state, resolve + persist, recent prefill |
| Edit | `ui/connection/ConnectionScreen.kt` | eye toggle + default-directory field |
| Edit | `ui/files/FilesViewModel.kt` | seed `loadFiles` from `SessionState` |
| Edit | tests (Profile, ConnectionVM, FilesVM) | new ctor arg + new cases |

## Risks
- `ConnectionViewModel`/`FilesViewModel` constructor signatures change → update all
  existing test constructions (8 + 7).
- `homeDirectory()` resolution can't be JVM-unit-tested against a real session;
  the resolve/fallback logic is covered in `ConnectionViewModelTest` via a mocked
  `homeDirectory()`.

## Dependencies
- 001 (Connect & Browse) — owns the connect form, `ISftpClient`, Files browser.

## ADR-007
Bridge Connect → Files with a small `@Singleton SessionState` (resolved remote
start directory) rather than NavHost arguments or a shared `SavedStateHandle`;
keeps the tab-based, no-NavHost architecture intact. Default directory is resolved
once at connect (`enteredDir` or `getHome()`), never auto-corrected on list
failure.
