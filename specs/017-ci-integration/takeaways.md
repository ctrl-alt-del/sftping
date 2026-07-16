# CI Integration — Takeaways

## What went well
- The entire CI integration is one file (.github/workflows/ci.yml) — no Makefile, no
  Docker image, no custom runner setup. GitHub Actions + the official Gradle action +
  `sdkmanager` handle everything.
- `actions/cache@v4` with a simple constant key makes the SDK download a one-time
  cost (~200 MB on first run, ~3 seconds on subsequent runs). This is the same
  pattern used by AndroidX and JetBrains Compose repos.

## What we learned / surprises
- GitHub's `ubuntu-latest` runner ships `cmdline-tools` in the pre-installed
  `$ANDROID_SDK_ROOT`, but only platforms up to ~35. For compileSdk 37, an explicit
  download is required — and Google's CDN is occasionally flaky. The cache
  pattern is the standard mitigation; without it, builds fail non-deterministically
  on network issues.
- `gradle/actions/setup-gradle@v4` is maintained by the Gradle team and handles
  caching, JVM args, and wrapper validation in one step. It replaces
  `gradle/gradle-build-action@v3` (now deprecated).
- `concurrency.cancel-in-progress: true` is essential for fast-feedback CI —
  without it, rapid pushes queue up and the last push's results are delayed by
  earlier, now-stale runs.

## Reusable patterns
- Isolated SDK root (`~`/`.android-sdk` inside the checkout workspace) keeps the
  cache small and avoids polluted cache keys from the runner's pre-installed SDK
  (which contains platforms 34–35 and other files we don't need).
- `cache-read-only: true` on PRs prevents cache poisoning from untrusted
  branches while still accelerating PR builds with the `master` cache.

## To add later
- Instrumented tests (emulator job) — when tests exist in `app/src/androidTest/`.
- Release build job (e.g. signed APK generation) — when the app is ready for
  distribution.
- Code coverage (JaCoCo) upload to a service like Codecov or SonarCloud.
