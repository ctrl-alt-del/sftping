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
JDK 21 (Temurin), downloads Android platform 37 from the repo's `ci-assets`
branch, caches both the platform (~164 MB uncompressed) and Gradle artefacts,
then runs `lint`, `assembleDebug`, and `testDebug`.

**SDK caching strategy:** Platform 37 is not yet in Google's public SDK
repository (sdkmanager cannot install it). A compressed copy (~60 MB) is
hosted in the `ci-assets` branch; CI downloads and extracts it into the
runner's pre-installed SDK on cache miss. The uncompressed `android-37.0`
directory (~164 MB) is cached via `actions/cache@v4` keyed
`android-sdk-37-linux-v4`. A symlink `android-37 → android-37.0` is created
on every run for AGP 9.x. When Google eventually publishes the platform, this
can be replaced with a single `sdkmanager` call.

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

- **Platform 37 not in public SDK repository:** `sdkmanager` cannot install it
  (exit code 8: package not found). Mitigated by hosting the platform in the
  `ci-assets` branch and caching it via `actions/cache@v4`. When Google
  publishes it publicly, replace with `sdkmanager`.
- **GitHub raw URL may be slow or unreachable:** Mitigated by `actions/cache@v4`
  — the last-good cached platform is restored even if the download fails.
- **Gradle OOM:** AGP 9.x + Compose BOM uses significant heap. Mitigated by
  `gradle/actions/setup-gradle@v4` default JVM args; can add explicit `-Xmx4g`
  if needed.

## Dependencies
None — pure DevOps addition, no code changes.
