# Requirements Document

## Introduction

Dieses Dokument spezifiziert die Anforderungen für das Feature „Notification Permission Toggle" in EchoList. Das Feature erweitert den MainTaskSettingsScreen um einen Toggle, mit dem Notifications pro MainTask aktiviert oder deaktiviert werden können. Wenn der Benutzer den Screen mit aktivierten Notifications verlässt und eine aktive Wiederholung (Recurrence) konfiguriert ist, prüft das System die Notification-Berechtigung der Plattform und fragt diese gegebenenfalls beim Benutzer an. So wird sichergestellt, dass Notifications nur geplant werden, wenn die Berechtigung tatsächlich erteilt wurde.

## Glossary

- **MainTaskSettingsScreen**: Der Compose-Screen, auf dem Benutzer Fälligkeitsdatum, Wiederholungsintervall und Wiederholungsdetails für einen MainTask konfigurieren.
- **MainTaskSettingsViewModel**: Das ViewModel, das den UI-State des MainTaskSettingsScreen verwaltet und Änderungen über den MainTaskSettingsResultBus emittiert.
- **MainTaskSettingsResultBus**: Ein SharedFlow-Bus, über den Einstellungsänderungen vom MainTaskSettingsScreen an das EditTaskListViewModel zurückfließen.
- **NotificationToggle**: Ein UI-Toggle-Element im MainTaskSettingsScreen, das anzeigt, ob Notifications für den jeweiligen MainTask aktiviert sind.
- **NotificationPermissionChecker**: Eine plattformunabhängige Schnittstelle, die den aktuellen Status der Notification-Berechtigung abfragt.
- **NotificationPermissionRequester**: Eine plattformunabhängige Schnittstelle, die den Benutzer um die Notification-Berechtigung bittet und das Ergebnis zurückgibt.
- **NotificationScheduler**: Die bestehende plattformunabhängige Schnittstelle zum Planen und Abbrechen lokaler Notifications.
- **MainTask**: Das Domain-Modell eines Haupttasks mit Feldern für ID, Beschreibung, Status, Fälligkeitsdatum, Wiederholung und Subtasks.
- **RecurrenceState**: Der UI-State der Wiederholungskonfiguration (Off, Daily, Weekly, Monthly, Yearly).
- **Aktive_Wiederholung**: Ein Zustand, in dem der RecurrenceState ungleich RecurrenceState.Off ist.
- **scheduleTaskNotification**: Die bestehende Funktion, die basierend auf Fälligkeitsdatum und Wiederholung entscheidet, ob eine Notification geplant oder abgebrochen wird.

## Requirements

### Requirement 1: Notification-Toggle im MainTaskSettingsScreen

**User Story:** Als Benutzer möchte ich im MainTaskSettingsScreen einen Toggle haben, mit dem ich Notifications für einen einzelnen MainTask ein- oder ausschalten kann, damit ich selbst entscheiden kann, ob ich für diesen Task erinnert werden möchte.

#### Acceptance Criteria

1. THE MainTaskSettingsScreen SHALL einen NotificationToggle anzeigen, der den aktuellen Notification-Status des MainTasks widerspiegelt.
2. WHEN der MainTaskSettingsScreen geöffnet wird und kein vorheriger Notification-Status für den MainTask gespeichert ist, THE NotificationToggle SHALL den Defaultwert „aktiviert" anzeigen.
3. WHEN der Benutzer den NotificationToggle betätigt, THE MainTaskSettingsViewModel SHALL den neuen Toggle-Status im UI-State aktualisieren und über den MainTaskSettingsResultBus emittieren.
4. WHILE der RecurrenceState gleich RecurrenceState.Off ist, THE NotificationToggle SHALL deaktiviert (ausgegraut, nicht interaktiv) dargestellt werden.
5. WHEN der RecurrenceState von einer aktiven Wiederholung auf RecurrenceState.Off wechselt, THE MainTaskSettingsViewModel SHALL den NotificationToggle-Status automatisch auf „deaktiviert" setzen.

### Requirement 2: Persistierung des Notification-Status pro MainTask

**User Story:** Als Benutzer möchte ich, dass meine Notification-Einstellung pro Task gespeichert wird, damit die Einstellung beim erneuten Öffnen des Screens erhalten bleibt.

#### Acceptance Criteria

1. THE MainTask-Domain-Modell SHALL ein Feld „isNotificationEnabled" vom Typ Boolean enthalten, das angibt, ob Notifications für diesen Task aktiviert sind.
2. WHEN ein MainTask ohne gespeicherten Notification-Status erstellt wird, THE System SHALL den Defaultwert „true" (aktiviert) für isNotificationEnabled verwenden.
3. WHEN der Benutzer den NotificationToggle ändert, THE MainTaskSettingsResultBus SHALL den aktuellen isNotificationEnabled-Wert als Teil des MainTaskSettingsResult emittieren.
4. WHEN der MainTaskSettingsScreen erneut für denselben MainTask geöffnet wird, THE MainTaskSettingsViewModel SHALL den gespeicherten isNotificationEnabled-Wert aus den Route-Parametern laden und im NotificationToggle anzeigen.

### Requirement 3: Berechtigungsprüfung beim Verlassen des Screens

**User Story:** Als Benutzer möchte ich, dass die App die Notification-Berechtigung prüft, bevor eine Notification geplant wird, damit ich nicht überrascht werde und die Kontrolle über meine Systemeinstellungen behalte.

#### Acceptance Criteria

1. WHEN der Benutzer den MainTaskSettingsScreen verlässt und der NotificationToggle aktiviert ist und eine aktive Wiederholung konfiguriert ist, THE MainTaskSettingsViewModel SHALL den NotificationPermissionChecker aufrufen, um den aktuellen Berechtigungsstatus abzufragen.
2. WHILE die Notification-Berechtigung bereits erteilt ist, THE MainTaskSettingsViewModel SHALL den Screen ohne weitere Benutzerinteraktion verlassen und das Ergebnis über den ResultBus emittieren.
3. IF die Notification-Berechtigung nicht erteilt ist und der NotificationToggle aktiviert ist und eine aktive Wiederholung konfiguriert ist, THEN THE MainTaskSettingsViewModel SHALL den NotificationPermissionRequester aufrufen, um den Benutzer um die Berechtigung zu bitten.
4. WHILE der NotificationToggle deaktiviert ist oder keine aktive Wiederholung konfiguriert ist, THE MainTaskSettingsViewModel SHALL den Screen ohne Berechtigungsprüfung verlassen.

### Requirement 4: Verhalten nach Berechtigungsanfrage

**User Story:** Als Benutzer möchte ich, dass die App angemessen reagiert, wenn ich die Notification-Berechtigung gewähre oder verweigere, damit meine Entscheidung respektiert wird.

#### Acceptance Criteria

1. WHEN der Benutzer die Notification-Berechtigung über den NotificationPermissionRequester gewährt, THE MainTaskSettingsViewModel SHALL den NotificationToggle auf „aktiviert" belassen und das Ergebnis mit isNotificationEnabled = true über den ResultBus emittieren.
2. WHEN der Benutzer die Notification-Berechtigung über den NotificationPermissionRequester verweigert, THE MainTaskSettingsViewModel SHALL den NotificationToggle auf „deaktiviert" setzen und das Ergebnis mit isNotificationEnabled = false über den ResultBus emittieren.
3. IF der NotificationPermissionRequester einen Fehler zurückgibt (z.B. plattformspezifischer Fehler), THEN THE MainTaskSettingsViewModel SHALL den NotificationToggle auf „deaktiviert" setzen und das Ergebnis mit isNotificationEnabled = false emittieren, ohne eine Exception zu werfen.

### Requirement 5: Integration mit bestehendem Notification-Scheduling

**User Story:** Als Benutzer möchte ich, dass die bestehende Notification-Logik das isNotificationEnabled-Flag respektiert, damit keine ungewollten Notifications geplant werden.

#### Acceptance Criteria

1. WHEN die Funktion scheduleTaskNotification aufgerufen wird und der MainTask das Feld isNotificationEnabled = false hat, THE scheduleTaskNotification SHALL die Notification für diesen Task abbrechen (cancel) und keine neue Notification planen.
2. WHEN die Funktion scheduleTaskNotification aufgerufen wird und der MainTask das Feld isNotificationEnabled = true hat, THE scheduleTaskNotification SHALL das bisherige Verhalten beibehalten (Notification planen bei gültigem Fälligkeitsdatum und aktiver Wiederholung, abbrechen bei fehlendem Fälligkeitsdatum oder fehlender Wiederholung).
3. WHEN der NotificationToggle von „aktiviert" auf „deaktiviert" geändert wird, THE EditTaskListViewModel SHALL beim nächsten Sync-Vorgang die bestehende Notification für den betroffenen MainTask abbrechen.

### Requirement 6: Plattformübergreifende Berechtigungsschnittstelle

**User Story:** Als Entwickler möchte ich eine einheitliche Schnittstelle für die Berechtigungsprüfung und -anfrage, damit die plattformspezifischen Unterschiede gekapselt sind.

#### Acceptance Criteria

1. THE NotificationPermissionChecker SHALL eine suspend-Funktion `isGranted(): Boolean` bereitstellen, die den aktuellen Berechtigungsstatus der Plattform zurückgibt.
2. THE NotificationPermissionRequester SHALL eine suspend-Funktion `request(): Boolean` bereitstellen, die den Benutzer um die Berechtigung bittet und true zurückgibt bei Gewährung, false bei Verweigerung.
3. THE NotificationPermissionChecker und NotificationPermissionRequester SHALL über Koin als plattformspezifische Implementierungen bereitgestellt werden (expect/actual oder Interface-Binding pro Plattform-Modul).
4. WHEN auf Android die Berechtigung POST_NOTIFICATIONS (API 33+) nicht erteilt ist, THE Android-Implementierung von NotificationPermissionRequester SHALL den System-Berechtigungsdialog anzeigen.
5. WHEN auf iOS die Notification-Berechtigung nicht erteilt ist, THE iOS-Implementierung von NotificationPermissionRequester SHALL UNUserNotificationCenter.requestAuthorization aufrufen.
6. WHEN auf JVM Desktop die Berechtigung geprüft wird, THE JVM-Implementierung von NotificationPermissionChecker SHALL immer true zurückgeben (Desktop benötigt keine explizite Berechtigung).
7. WHEN auf Web (JS/WasmJS) die Berechtigung nicht erteilt ist, THE Web-Implementierung von NotificationPermissionRequester SHALL Notification.requestPermission() aufrufen.

### Requirement 7: UI-Darstellung des Toggles

**User Story:** Als Benutzer möchte ich, dass der Notification-Toggle visuell konsistent mit dem restlichen Design ist und klar kommuniziert, wann er verfügbar ist.

#### Acceptance Criteria

1. THE NotificationToggle SHALL als Material 3 Switch-Composable innerhalb einer eigenen SettingsSection mit dem Titel „Notifications" im MainTaskSettingsScreen dargestellt werden.
2. THE NotificationToggle SHALL die EchoListTheme-Tokens für Farben, Typografie und Abstände verwenden.
3. WHILE der NotificationToggle deaktiviert (ausgegraut) ist weil keine aktive Wiederholung konfiguriert ist, THE MainTaskSettingsScreen SHALL einen erklärenden Hilfetext anzeigen, der dem Benutzer mitteilt, dass Notifications nur bei aktiver Wiederholung verfügbar sind.
4. THE NotificationToggle-Section SHALL unterhalb der Wiederholungs-Section positioniert werden.
