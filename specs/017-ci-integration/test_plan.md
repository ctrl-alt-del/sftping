# CI Integration — Test Plan

## Verification
- Merge to `master` and check the GitHub Actions tab for a green run.
- Open a PR against `master` and verify the CI run triggers and completes.
- Verify the CI status badge in `README.md` renders correctly.

## Unit Tests
No code changes — the existing `./gradlew testDebug` suite validates the app
builds and passes. CI itself is the test harness addition.

## Manual smoke tests
- [ ] Push to master → Actions shows a green workflow run.
- [ ] PR → Actions shows a green run, cache is read-only.
- [ ] Delete the `android-sdk-37-linux-v1` cache key from
      `https://github.com/ctrl-alt-del/sftping/actions/caches` → next CI run
      re-downloads the SDK (cold cache path).
