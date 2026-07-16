# CI Integration — Tasks

## Block 0: Spec
- [x] `doc-coauthoring`: spec.md + plan.md
- [x] Mockup: skipped (no UI change)
- [x] `test_plan.md`: skipped (CI itself is the test harness; verified on GitHub after merge)

## Block 1: CI workflow
- [x] **Task 1.1**: Create `.github/workflows/ci.yml` with lint, assembleDebug,
      testDebug, SDK caching, Gradle caching — `.github/workflows/ci.yml`
  - Verify: merge to `master`, check GitHub Actions tab
- [x] **Task 1.2**: CIAgents.md CI section, README.md badge, MEMORY.md #build note
      + ownership, specs/index.md row 017
  - Verify: file review
