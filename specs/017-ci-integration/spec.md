# CI Integration — Specification

## User Story
As a developer, I want every push to `master` and every pull request to `master`
to be automatically built, linted, and tested, so that regressions are caught
before merge and the build stays green.

## UX/UI
No UI change. CI status badges in `README.md` and on PRs.

## Acceptance Criteria
- [ ] Every push to `master` triggers a CI run (lint + assemble + unit tests).
- [ ] Every pull request targeting `master` triggers a CI run.
- [ ] CI runs on `ubuntu-latest` with JDK 21 (Temurin), Gradle 9.4.1 via wrapper,
      Android compileSdk 37.
- [ ] SDK platform 37 is cached via `actions/cache@v4` so subsequent runs skip the
      Google CDN download (~150 MB cache, keyed `android-sdk-37-linux-v1`).
      Build-tools are fetched automatically by AGP 9.x — no version-pinning needed.
- [ ] Cache is read-only on PRs (PRs can read but not write the cache — prevents
      cache poisoning).
- [ ] Concurrent pushes cancel in-progress runs (`cancel-in-progress: true`).
- [ ] Failed builds upload test/lint reports as build artifacts (retained 7 days).
- [ ] Lint runs before assemble + test (fail fast).
- [ ] Instrumented tests (`connectedDebugAndroidTest`) are explicitly skipped
      (documented: no instrumented tests exist yet; add when they do).

## Non-Functional Requirements
- No secrets or tokens required (public repo).
- No cost (GitHub Actions is free for public repositories, cache limit 10 GB).
- Workflow timeout: 30 minutes (Android SDK download + Gradle build).
- Expected run time: ~3 minutes (warm cache) to ~5 minutes (cold cache).

## Out of Scope
- Release/versioned builds.
- Code coverage upload.
- Lint strictness enforcement (lint runs but doesn't block the build — treat as
  advisory for now; can tighten later).
- Auto-merge or deployment.
