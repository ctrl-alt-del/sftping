# Connect & Browse — Takeaways

## What Went Well
- The Hilt + KSP + JSch dependency chain resolved cleanly once version compatibility was sorted. `@HiltViewModel` +
  `viewModel()` (lifecycle-viewmodel-compose) with `@AndroidEntryPoint` on the Activity works without needing
  `hilt-navigation-compose` — one fewer dep, cleaner navigation.
- DataStore + `org.json` serialization was lighter than kotlinx-serialization and kept the commit focused.
- Host-key TOFU in the app layer (post-connect `session.hostKey` → fingerprint → KnownHostsStore) is cleaner
  than JSch's `HostKeyRepository` API; the UiState-driven dialog flow maps cleanly.

## What We Learned
- KSP versioning is independent of Kotlin (2.3.9 for Kotlin 2.2.10), not `<kotlin-version>-<ksp-version>`.
- Hilt's Gradle plugin needs ≥ 2.59.2 for AGP 9.x; lower versions crash with "Android BaseExtension not found."
- `compileSdk 37` is the minimum for `androidx.core:core-ktx:1.19.0` (the template shipped with both, but
  only 36 was set — a latent conflict).
- `@HiltAndroidApp` class name collided with the existing `fun SftpingApp()` composable in `MainActivity.kt` →
  renamed Application to `SftpingApplication`. Always scan for name collisions in the same package.
- JSch `HostKey.getKey()` returns a **base64-encoded String**, not raw bytes — must `Base64.getDecoder().decode()`
  before computing the SHA-256 fingerprint.
- `org.json.JSONObject` is available at runtime (Android framework) but absent in JVM test classpath →
  need `org.json:json` as `testImplementation`.

## API / Tech Surprises
- `mutableStateOf` in a ViewModel needs explicit compose runtime imports (`getValue`, `setValue`); IntelliJ
  typically auto-adds them, but pure CLI codepaths don't.
- Material 3 `NavigationSuiteScaffold` + `viewModel()` works without `navigation-compose` — no NavHost needed.

## Patterns Worth Reusing
- **Post-connect host-key verification**: connect first (JSch level), extract key, check store in app layer,
  return result as a sealed class → UiState reacts. Decouples JSch from UI threading concerns.
- **DataStore + JSON**: for simple list-of-objects storage, a single `stringPreferencesKey` with a JSON blob
  avoids Proto DataStore overhead.
- **Secrets outside ConnectionProfile**: credential encryption lives in `SecretStore` keyed by `host:port:user`,
  not bundled in the profile object — keeps DataStore records audit-clean.
