# Spec-Driven Development Workflow

## Phases

1. **Spec** — Write plan.md + spec.md + UX mockups. Review with stakeholder.
2. **Plan** — Break into tasks.md + test_plan.md. Estimate and order.
3. **Build** — Implement one task per commit. Verify build + tests at each commit.
4. **Ship** — Write takeaways.md. Merge to MEMORY.md.

## Skills to Use

### Spec Writing Phase
- **`doc-coauthoring`**: Invoke when starting spec.md or plan.md. Enforces a
  describe → review → approve → iterate loop. Prevents guessing requirements.

### Mockup Generation Phase
- **`canvas-design` + `theme-factory`**: Invoke when user describes a feature
  but has NO mockup. `canvas-design` generates visual layout; `theme-factory`
  applies the project's existing color scheme (see Design System below).
  Output to ux-ui/. Skip when user provides real screenshots — AI reads them
  natively.

### Interactive Prototyping Phase (optional)
- **`frontend-design`**: When a feature has complex interaction logic
  (navigation flow, state transitions). Generates an HTML/React prototype.
  For this Compose app, treat such prototypes as throwaway design references —
  the production UI is Jetpack Compose, not HTML.
- **`webapp-testing`**: Verifies an HTML prototype's interactions with
  Playwright. Use when a feature has >= 3 screens or multi-step flows that
  would be expensive to get wrong in native code.

### Reading Provided Screenshots
- No skill needed. AI reads real UI screenshots natively. Only use when the
  user provides actual images of their intended UI.

### Test Plan Authoring
- No skill needed. Apply standard practices:
  - AAA pattern (Arrange-Act-Assert)
  - Boundary testing
  - Equivalence partitioning
  - JUnit-based. Pure-logic tests go in `app/src/test/` (run on the JVM via
    `./gradlew testDebug`); UI/instrumented tests go in `app/src/androidTest/`
    (run on a device/emulator via `./gradlew connectedDebugAndroidTest`).
    Prefer pushing logic out of Composables into testable classes so most
    coverage lands in fast JVM unit tests.

## Skill Invocation Order

```
Feature Request Received
│
├── User has mockup?
│   ├── YES → AI reads natively → spec writing
│   └── NO  → canvas-design + theme-factory → generate mockup
│
├── doc-coauthoring → co-write spec.md + plan.md
│
├── Complex flow (>= 3 screens)?
│   ├── YES → frontend-design → HTML prototype
│   │         └── webapp-testing → verify interactions
│   └── NO  → skip
│
└── Implementation → tasks.md → one commit per task
```

## Conventions

- Folder names: `NNN-short-kebab-name/` (e.g., `001-sftp-connect/`)
- NNN is zero-padded sequential ID (001, 002, ...)
- Statuses: 📋 Planned → 🚧 In Progress → ✅ Done → 📦 Archived
- One task = one independent, build-verifiable, test-passing commit
- File conflicts surface via `touches` field in plan.md frontmatter + index.md

## Tech Stack

- **Stack**: Kotlin + Jetpack Compose (Android), single `:app` module.
- **Package**: `com.example.sftping`. UI entry point: `MainActivity.kt`.
- **Build system**: Gradle Kotlin DSL with a version catalog
  (`gradle/libs.versions.toml`) — add/bump dependencies there, not inline.
- Tooling is intentionally bleeding-edge (AGP 9.x, compileSdk 36, Compose BOM
  2025.x). When adding libraries, confirm versions are compatible with the
  catalog rather than copying older snippets from the web.

## Design System (for `theme-factory`)

Material 3 theme defined in `app/src/main/java/com/example/sftping/ui/theme/`.
Stock starter palette (`Color.kt`) — replace as the product brand develops:

| Role | Light | Dark |
|------|-------|------|
| Primary | Purple40 `#6650A4` | Purple80 `#D0BCFF` |
| Secondary | PurpleGrey40 `#625B71` | PurpleGrey80 `#CCC2DC` |
| Tertiary | Pink40 `#7D5260` | Pink80 `#EFB8C8` |

`Theme.kt` currently enables dynamic color on Android 12+, so on-device colors
may derive from the user's wallpaper rather than this palette.

## Build & Test Commands

- Build: `./gradlew assembleDebug`
- Test: `./gradlew testDebug`
- Instrumented/UI test: `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint`

These commands are used in tasks.md for each task's verification step.
