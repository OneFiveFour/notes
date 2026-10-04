# EchoList

Kotlin Multiplatform productivity app for Android, Desktop/JVM and Web (JavaScript and WasmJS).

The app browses folders and edits notes and task lists against a self-hosted backend.

## Project layout

- `apps/android`, `apps/desktop`, `apps/web`: platform entrypoints and packaging.
- `app`: shared composition root, authentication state and navigation.
- `features/browser`, `features/tasklist`, `features/note`, `features/login`, `features/tasksettings`: independently buildable libraries.
- `core`: focused shared libraries for tasks, recurrence, notifications, files, session, networking, protocol, database and design system.
- `build-logic`: shared build conventions and automated architecture checks.

See [ARCHITECTURE.md](ARCHITECTURE.md) for responsibilities, dependency rules, package layers and guidance for adding features.

## Build and run

Use `./gradlew` on Unix or `.\gradlew.bat` on Windows, followed by the tasks below.

| Purpose | Task |
|---|---|
| Architecture boundaries | `verifyArchitecture` |
| All module JVM tests | `jvmTest` |
| Browser database tests (Chrome required) | `:core:database:jsBrowserTest :core:database:wasmJsBrowserTest` |
| Build one feature and its core dependencies | `:features:browser:assemble` |
| Test one feature | `:features:note:jvmTest` |
| Android debug APK | `:apps:android:assembleDebug` |
| Desktop application | `:apps:desktop:run` |
| Desktop distributable | `:apps:desktop:createDistributable` |
| Web development (JS) | `:apps:web:jsBrowserDevelopmentRun` |
| Web development (WasmJS) | `:apps:web:wasmJsBrowserDevelopmentRun` |
| Web production (JS) | `:apps:web:jsBrowserDistribution` |
| Web production (WasmJS) | `:apps:web:wasmJsBrowserDistribution` |

Android builds require a configured Android SDK. Desktop distributions require a JDK with `jpackage`; installer formats depend on the host OS. Web builds manage their Node/Yarn dependencies through Gradle and checked-in lockfiles.

Release helpers are `release-build.ps1` and `release-build.sh`. They run the architecture check and all JVM tests unless tests are explicitly skipped.

## Development

Feature packages follow `data`, `di`, `domain`, `ui`; create a layer only when it owns code. Domain contracts are independent of UI and infrastructure. Keep implementation classes internal and navigation in the app. Shared UI uses `EchoListTheme` from `core:designsystem`.

Protocol schemas live in `proto`; generated Wire classes belong to `core:protocol`. SQLDelight schema and platform drivers belong to `core:database`. Never edit generated output.

Historical requirements remain in `.kiro/specs`. Current architecture takes precedence over their former single-module layout. iOS is no longer a supported target.
