# Release Build

This project can build Android, Desktop, JS, and Wasm release artifacts from the repository root.

## Prerequisites

- JDK 17 or newer on `PATH`
- Android SDK installed and configured through `local.properties`
- Gradle wrapper available as `gradlew.bat` or `gradlew`
- For iOS releases: macOS with Xcode. iOS archive/export is handled from `iosApp` in Xcode.

## Android signing

Android release builds should be signed before distribution.

1. Copy the example file:

   ```powershell
   Copy-Item keystore.properties.example keystore.properties
   ```

2. Create a release keystore, or let the PowerShell script create one:

   ```powershell
   .\release-build.ps1 -Target android -GenerateKeystore
   ```

3. Check `keystore.properties` and keep it private. It is ignored by git.

## Build commands

Windows:

```powershell
.\release-build.ps1 -Target android
.\release-build.ps1 -Target desktop
.\release-build.ps1 -Target web
.\release-build.ps1 -Target all
```

macOS/Linux:

```bash
./release-build.sh android
./release-build.sh desktop
./release-build.sh web
./release-build.sh all
```

Use `-UnsignedAndroid` in PowerShell or `--unsigned-android` in Bash only for local validation builds that will not be distributed.

## Output locations

- Android APK: `composeApp/build/outputs/apk/release/`
- Android AAB: `composeApp/build/outputs/bundle/release/`
- Desktop packages: `composeApp/build/compose/binaries/main/`
- JS distribution: `composeApp/build/dist/js/productionExecutable/`
- Wasm distribution: `composeApp/build/dist/wasmJs/productionExecutable/`

## Direct Gradle tasks

```powershell
.\gradlew.bat :composeApp:assembleRelease :composeApp:bundleRelease
.\gradlew.bat :composeApp:packageReleaseDistributionForCurrentOS
.\gradlew.bat :composeApp:jsBrowserDistribution
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
```
