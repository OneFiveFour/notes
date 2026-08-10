# Release Build – Lokale Einrichtung

## Voraussetzungen

- Android SDK installiert (über `local.properties` konfiguriert)
- Java 11+
- Ein Release-Keystore (`.jks` oder `.keystore`)

## 1. Keystore erstellen (einmalig)

Falls noch kein Keystore vorhanden ist:

```bash
keytool -genkeypair \
  -alias echolist \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore release-keystore.jks \
  -storepass <dein_passwort> \
  -keypass <dein_passwort>
```

Den Keystore im Projekt-Root ablegen (oder einen beliebigen Pfad wählen).

## 2. signing.properties anlegen

Kopiere die Vorlage und trage deine Werte ein:

```bash
cp signing.properties.template signing.properties
```

Dann `signing.properties` editieren:

```properties
RELEASE_STORE_FILE=release-keystore.jks
RELEASE_STORE_PASSWORD=dein_keystore_passwort
RELEASE_KEY_ALIAS=echolist
RELEASE_KEY_PASSWORD=dein_key_passwort
```

> **Wichtig:** `signing.properties` und `*.jks`/`*.keystore` sind in `.gitignore` eingetragen und werden nicht ins Repository committed.

## 3. Release APK bauen

```bash
./gradlew :composeApp:assembleRelease
```

Die signierte APK liegt unter:
```
composeApp/build/outputs/apk/release/composeApp-release.apk
```

## 4. Release AAB bauen (für Play Store)

```bash
./gradlew :composeApp:bundleRelease
```

Das signierte Bundle liegt unter:
```
composeApp/build/outputs/bundle/release/composeApp-release.aab
```

## 5. Desktop Release

```bash
./gradlew :composeApp:packageReleaseMsi        # Windows
./gradlew :composeApp:packageReleaseDmg        # macOS
./gradlew :composeApp:packageReleaseDeb        # Linux
```

Alternativ für die aktuelle Plattform:

```bash
./gradlew :composeApp:createReleaseDistributable
```

## Dateiübersicht

| Datei | Zweck | Im Git? |
|-------|-------|---------|
| `signing.properties.template` | Vorlage mit Platzhaltern | ✓ |
| `signing.properties` | Echte Credentials | ✗ |
| `release-keystore.jks` | Keystore-Datei | ✗ |
| `composeApp/proguard-rules.pro` | R8/ProGuard-Regeln | ✓ |

## Fehlerbehebung

**Build schlägt fehl mit "signing config not found":**
→ Prüfe, ob `signing.properties` im Projekt-Root existiert und alle vier Werte gesetzt sind.

**APK ist nicht signiert:**
→ Stelle sicher, dass der Pfad in `RELEASE_STORE_FILE` korrekt ist (relativ zum Projekt-Root).

**R8-Fehler zur Laufzeit (ClassNotFoundException etc.):**
→ Ergänze fehlende Keep-Regeln in `composeApp/proguard-rules.pro`.
