---
feature_id: "017"
name: "CI Integration (GitHub Actions)"
status: "✅ Done"
depends_on: []
touches:
  - ".github/workflows/ci.yml"
  - "AGENTS.md"
  - "README.md"
  - "MEMORY.md"
  - "specs/index.md"
created: "2026-07-16"
---

# CI Integration — Plan

## Approach

Add a single GitHub Actions workflow (`.github/workflows/ci.yml`) that runs on
every push to `master` and every PR targeting `master`. The workflow sets up
JDK 21 (Temurin), downloads the Android SDK platform 37 + build-tools 37.0.0,
caches both the SDK (~200 MB) and Gradle artefacts, then runs `lint`,
`assembleDebug`, and `testDebug`.

**SDK caching strategy:** GitHub runners do not yet pre-install platform 37.
We download the `cmdline-tools`, accept licenses, install `platforms;android-37`
and `build-tools;37.0.0` into an isolated `ANDROID_SDK_ROOT`, and cache that
directory. The download only runs on cache miss (first run or eviction). When
GitHub eventually pre-installs API 37, the cache step can be removed — the
workflow degrades gracefully to a no-op download.

**No instrumented tests** — the project has no tests in `app/src/androidTest/`
currently. The workflow documents how to add them when needed.

## Files

| Action | File | Purpose |
|--------|------|---------|
| Create | `.github/workflows/ci.yml` | CI workflow |
| Create | `specs/017-ci-integration/spec.md` | Specification |
| Create | `specs/017-ci-integration/plan.md` | This plan |
| Create | `specs/017-ci-integration/tasks.md` | Task list |
| Create | `specs/017-ci-integration/takeaways.md` | Takeaways |
| Update | `AGENTS.md` | CI commands section |
| Update | `README.md` | CI status badge |
| Update | `MEMORY.md` | #build note + ownership |
| Update | `specs/index.md` | Row 017 |

## Risks

- **Google CDN flakiness:** `sdkmanager` download may time out. Mitigated by
  `actions/cache@v4` — the last-good cached SDK is restored even if Google's
  servers are down for a given run.
- **`build-tools;37.0.0` may not exist:** If Google hasn't released this exact
  version, the `sdkmanager` step fails. Fix: don't pin a specific build-tools
  version — install only the platform and let AGP 9.x automatically fetch the
  build-tools it needs on first use.
- **Gradle OOM:** AGP 9.x + Compose BOM uses significant heap. Mitigated by
  `gradle/actions/setup-gradle@v4` default JVM args; can add explicit `-Xmx4g`
  if needed.

## Dependencies
None — pure DevOps addition, no code changes.
