# sftping — Agent Guide

## Project Summary

`sftping` is an Android app (intended as an SFTP client) built with **Kotlin +
Jetpack Compose**. Single `:app` module, package `com.example.sftping`, UI entry
point `MainActivity.kt`. Build system is Gradle Kotlin DSL with a version
catalog at `gradle/libs.versions.toml`.

## Commands

- Build: `./gradlew assembleDebug`
- Unit tests (JVM): `./gradlew testDebug`
- Instrumented/UI tests (device/emulator): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint`

## Conventions

- Add/bump dependencies in `gradle/libs.versions.toml`, not inline in
  `build.gradle.kts`.
- Pure logic lives in testable classes (covered by `app/src/test/`); keep
  Composables thin. Instrumented/UI tests live in `app/src/androidTest/`.
- Never commit secrets — `local.properties`, keystores, and `.env`-style files
  are gitignored; keep it that way.

## Triggering Feature Development

When the user describes a new feature (creates, builds, adds, wants a new screen,
etc.), follow the spec-driven development workflow in `specs/SDD.md`. Read
`MEMORY.md` before writing any spec to avoid repeating known bugs. The workflow:
1. Generate mockups if needed (`canvas-design` + `theme-factory`)
2. Co-author spec + plan (`doc-coauthoring`)
3. Write test plan and tasks
4. Implement one commit per task
5. Write takeaways → promote to `MEMORY.md`
