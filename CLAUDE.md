# EchoList Repository Notes

Dieses Repository ist ein Kotlin-Multiplatform-Projekt mit Compose Multiplatform.

## Projektstruktur

- `composeApp/`: Hauptmodul mit Shared UI und Plattform-spezifischem Code
- `iosApp/`: natives iOS-Xcode-Projekt als Wrapper/Entry Point
- `proto/`: Protobuf-Schemas fuer Wire-Codegenerierung
- `docs/`: projektspezifische Migrations- und Architekturhinweise

## Tech-Stack

- Kotlin Multiplatform
- Compose Multiplatform + Material 3
- Koin fuer Dependency Injection
- Ktor Client fuer Networking
- SQLDelight fuer lokale Persistenz
- Wire fuer Protobuf-Modelle
- Kotlinx Serialization
- Kotest + Kotlin Test fuer Tests
- Detekt + Ktlint fuer statische Analyse und Formatregeln

## Wichtige Module und Entry Points

- Android Entry Point: `composeApp/src/androidMain/kotlin/net/onefivefour/echolist/MainActivity.kt`
- Android Application Class: `composeApp/src/androidMain/kotlin/net/onefivefour/echolist/EchoListApplication.kt`
- Desktop Entry Point: `composeApp/src/jvmMain/kotlin/net/onefivefour/echolist/MainApp.kt`
- Web Entry Point: `composeApp/src/webMain/kotlin/net/onefivefour/echolist/Main.kt`
- iOS Swift Entry Point: `iosApp/iosApp/iOSApp.swift`
- iOS Compose Bridge: `composeApp/src/iosMain/kotlin/net/onefivefour/echolist/MainViewController.kt`

## Build- und Run-Kommandos

Unter Windows:

- Android Debug Build: `.\gradlew.bat :composeApp:assembleDebug`
- Desktop App starten: `.\gradlew.bat :composeApp:run`
- Web Wasm starten: `.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun`
- Web JS starten: `.\gradlew.bat :composeApp:jsBrowserDevelopmentRun`

## Qualitaetssicherung

- Gesamttests: `.\gradlew.bat test`
- JVM-Tests: `.\gradlew.bat :composeApp:jvmTest`
- Detekt: `.\gradlew.bat detekt`
- Ktlint: `.\gradlew.bat ktlintCheck`

## Wichtige Hinweise fuer zukuenftige Arbeit

- `settings.gradle.kts` bindet nur `:composeApp` in Gradle ein. `iosApp` wird separat in Xcode gebaut.
- Wire generiert hier nur Message-Typen. Service-Generierung ist absichtlich deaktiviert.
- Es gibt `jsMain`, `wasmJsMain` und zusaetzlich ein `webMain` Source Set. Bei Web-Aenderungen immer pruefen, welches Source Set wirklich verwendet wird.
- Android-Initialisierung kann ueber `EchoListApplication` laufen, nicht nur ueber `MainActivity`.
- Statische Analyse ist streng konfiguriert: `detekt` und `ktlint` schlagen bei Verstoessen fehl.

## Arbeitskonventionen

- Bevorzugt bestehende Patterns in `composeApp/src/commonMain` erweitern statt plattformspezifische Duplikate einzufuehren.
- Bei Datenmodell-Aenderungen auch `proto/` und SQLDelight-Schemas mitpruefen.
- Bei Aenderungen an Dependency Injection Koin-Setup und Plattforminitialisierung zusammen betrachten.
- Vor Abschluss von Code-Aenderungen nach Moeglichkeit passende Gradle-Checks fuer den betroffenen Bereich ausfuehren.
