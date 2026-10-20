# Implementierungsplan: Upcoming Main Tasks

Stand: 20.10.2026. Grundlage: aktueller unstaged Diff von `proto/tasks.proto`, Moduldefinitionen und bestehende Implementierung in `core:tasks`. Die nachfolgend geplante Data-/Domain-Anbindung ist umgesetzt; die separate UI und die offenen Backend-Fragen bleiben außerhalb dieses Schritts. Prüfergebnisse stehen am Dokumentende.

## 1. Befund und Umfang

Der Diff ergänzt genau einen RPC:

`/tasks.v1.TaskListService/ListUpcomingMainTasks`

- Request: `ListUpcomingMainTasksRequest(from_date)`; Pflichtwert, inklusive Untergrenze als UTC-Kalenderdatum im Format `YYYY-MM-DD`.
- Response: `ListUpcomingMainTasksResponse(tasks)` mit `UpcomingMainTask(task_id, title, due_date)`.
- `due_date` ist ein gespeichertes UTC-Kalenderdatum ohne Zeitzonenumrechnung.
- `GetMainTask` ist bereits in Remote Data Source, Mapper und Repository implementiert. Die Delete-Nachrichten wurden im Proto lediglich verschoben; ihre Felder und RPC-Signatur bleiben gleich.

Die Integration gehört in `core:tasks`, damit mehrere Features dieselben Verträge verwenden können. `core:protocol` generiert die neuen Transporttypen. Für diese Erweiterung sind weder ein neues Gradle-Modul noch zusätzliche Modulabhängigkeiten erforderlich. Die bestehende `tasksModule`-Verdrahtung bleibt ausreichend.

## 2. Domain-Vertrag

Neue öffentliche Datei unter `core/tasks/src/commonMain/kotlin/net/onefivefour/echolist/core/tasks/domain/model/UpcomingMainTask.kt`:

```kotlin
data class UpcomingMainTask(
    val taskId: String,
    val title: String,
    val dueDate: LocalDate
)
```

`LocalDate` stammt aus `kotlinx.datetime`, das `core:tasks` bereits als API-Abhängigkeit bereitstellt. Das Datum bezeichnet einen Kalendertag, keinen Zeitpunkt. Das bestehende `MainTask`-Modell mit String-Datum bleibt unverändert; eine allgemeine Datumsmigration wäre ein eigenes Vorhaben.

`domain/repository/TaskListRepository.kt` erhält:

```kotlin
suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>>
```

Das Datum ist explizit und ohne Defaultwert. So bleibt das Repository frei von versteckter Systemzeit und ist deterministisch testbar. Der spätere Aufrufer ermittelt für die Standardabfrage das heutige Datum mit einer testbaren Uhr in `TimeZone.UTC`. Keine lokale Systemzeitzone verwenden.

Das Ergebnis ist eine eigene Projektion: Es fehlen unter anderem Erledigungsstatus, Unteraufgaben, Wiederholung und Tasklisten-ID. Deshalb keine künstlichen vollständigen `MainTask`-Objekte mit erfundenen Standardwerten erzeugen. Auch `title` bleibt unverändert; seine genaue fachliche Bedeutung ist im Proto nicht erläutert.

## 3. Data-Layer

Alle nachstehenden Produktionsdateien liegen unter `core/tasks/src/commonMain/kotlin/net/onefivefour/echolist/core/tasks/`.

### Remote Data Source

`data/source/network/TaskListRemoteDataSource.kt` erhält eine interne Methode mit den generierten Request-/Response-Typen:

```kotlin
suspend fun listUpcomingMainTasks(
    request: ListUpcomingMainTasksRequest
): ListUpcomingMainTasksResponse
```

`TaskListRemoteDataSourceImpl.kt` implementiert sie nach bestehendem Muster über `ConnectRpcClient.call`, den vollständigen RPC-Pfad und die beiden Wire-`ADAPTER`. Transportfehler laufen durch den bestehenden Client. Authentifizierung, Protokoll-Header, Logging und Retry-Verhalten werden wiederverwendet.

### Mapping

`data/mapper/TaskListMapper.kt` erhält Überladungen für einen Proto-`UpcomingMainTask` und die Listenresponse. Bei Namenskonflikten Proto-Typ qualifizieren oder per Import-Alias unterscheiden.

- `task_id` unverändert nach `taskId`, `title` unverändert nach `title` kopieren.
- `due_date` strikt als ISO-Kalenderdatum lesen; leere oder ungültige Werte als fehlerhafte Serverantwort behandeln. Falls `LocalDate.parse` zusätzliche ISO-Formen akzeptiert, das zugesicherte `YYYY-MM-DD`-Format zusätzlich prüfen.
- Keine Umrechnung über `Instant`, lokale Mitternacht oder Systemzeitzone.
- Leere Liste ergibt eine erfolgreiche leere Domain-Liste.
- Reihenfolge beibehalten; keine zusätzliche Sortierung, Filterung oder Deduplizierung ohne fachliche Vorgabe.
- Bei einem ungültigen Listeneintrag die gesamte Abfrage fehlschlagen lassen, damit Aufgaben nicht stillschweigend verschwinden. Empfehlung: `NetworkException.SerializationError` mit Feldkontext und Ursache; dieser Fehlertyp bleibt im Data-Layer.

### Repository

`data/repository/TaskListRepositoryImpl.kt` implementiert die neue Methode auf dem bereits injizierten Dispatcher:

1. `fromDate` im geforderten vierstelligen ISO-Jahresformat serialisieren; außerhalb des durch `YYYY-MM-DD` darstellbaren Bereichs lokal mit `Result.failure` abbrechen.
2. `ListUpcomingMainTasksRequest(from_date = ...)` erstellen.
3. Remote Data Source einmal aufrufen und Antwort vollständig mappen.
4. `Result.success(mappedTasks)` bzw. `Result.failure(error)` zurückgeben.
5. `CancellationException` vor dem allgemeinen Exception-Catch erneut werfen. Das bestehende breite Catch-Muster nicht ungeprüft kopieren; eine umfassende Bereinigung anderer Methoden ist separat zu behandeln.

Der Lesezugriff löst kein `DirectoryChangeNotifier`-Event aus. Taskdaten sind aktuell nicht gecacht; daher keine SQLDelight-Migration, Offline-Queue oder neue Cache-Abstraktion einführen. Die Abfrage soll direkt den serverseitigen Endpoint nutzen, ohne Tasklisten einzeln nachzuladen.

## 4. Protokollgenerierung und Test-Doubles

- Wire über den vorhandenen Build von `core:protocol` ausführen. Die neue Methode benötigt Nachrichtenklassen, keinen generierten Service-Client; das bestehende Pruning von `tasks.v1.TaskListService` bleibt bestehen.
- `core:tasks/commonTest/.../FakeTaskListRemoteDataSource.kt` um konfigurierbares Ergebnis und aufgezeichneten Request erweitern.
- Weitere Implementierungen von `TaskListRemoteDataSource` anpassen, insbesondere `ThrowingDataSource` in `core:tasks/src/jvmTest/.../TaskListRepositoryPropertyTest.kt`.
- Sämtliche Implementierungen und Test-Doubles von `TaskListRepository` suchen und ergänzen, insbesondere in `features:tasklist` und den Notification-Tests in `core:tasks`. Kein Default im Produktionsinterface, der fehlende Implementierungen durch eine erfolgreiche leere Liste verdeckt.
- Die Koin-Bindings bleiben unverändert, da bestehende Implementierungen erweitert werden. Den vorhandenen DI-Test in `app` als Regression ausführen.

## 5. Gezielte Tests und Abnahme

Neue Tests bei den jeweiligen Besitzern in `core:tasks`:

| Bereich | Relevante Fälle |
|---|---|
| Mapper | Alle Felder, mehrere Einträge, Reihenfolge, leere Liste, Schaltjahrdatum, ungültiges oder leeres Datum, ein ungültiger Eintrag unter gültigen |
| Repository | Exaktes `from_date`, ISO-Format und Jahresbereich, erfolgreiche Liste, leere Liste, Transportfehler, Mappingfehler, Cancellation, kein Verzeichnisänderungs-Event |
| Remote Data Source | Aufgezeichneter RPC-Pfad; Request über echten Wire-Adapter dekodieren, Response mit echtem Adapter kodieren und vom getesteten Deserializer lesen lassen; Fehlerweitergabe |
| Späterer Aufrufer | UTC-Tageswechsel mit fester Uhr testen: lokaler Kalendertag kann vom UTC-Datum abweichen |

Für den Remote-Test reicht ein aufzeichnender `ConnectRpcClient` im selben Modul; dessen HTTP-Implementierung ist in `core:networking` intern. So prüfen wir den tatsächlichen RPC-Vertrag ohne Sichtbarkeiten für Tests aufzuweichen.

Nach Umsetzung auf Windows:

```powershell
.\gradlew.bat verifyArchitecture :core:tasks:jvmTest :features:tasklist:jvmTest :app:jvmTest
.\gradlew.bat :core:tasks:compileKotlinJs :core:tasks:compileKotlinWasmJs
.\gradlew.bat :apps:android:assembleDebug
```

Die JVM-/Kompilierungsaufgaben erzeugen auch die benötigten Wire-Nachrichten. Anschließend root `jvmTest` für den vollständigen JVM-Regressionslauf ausführen. Plattformprüfungen setzen die jeweiligen lokalen Toolchains voraus; tatsächliche Ausführung und Ergebnisse bei der Implementierung dokumentieren.

## 6. Backend-Semantik und spätere UI

Diese Punkte sind im Proto nicht zugesichert und dürfen nicht als Clientverhalten erfunden werden:

- Sortierung sowie Behandlung erledigter Aufgaben und wiederkehrender Aufgaben.
- Fachliche Bedeutung von `title`.
- Ob die inklusive Untergrenze fachlich tatsächlich alle überfälligen Aufgaben ausschließen soll. Mit heutigem `from_date` lassen sich frühere Fälligkeiten nach dem dokumentierten Untergrenzenvertrag nicht abrufen.
- Die Antwort enthält keine Tasklisten-ID und keinen Ordnerpfad. `getMainTask(taskId)` liefert zwar Aufgabendetails, aber ebenfalls keine Tasklistenzuordnung. Für das Öffnen des bisherigen Tasklisteneditors oder Änderungen über `UpdateTaskList` wäre diese Zuordnung zusätzlich nötig.

Diese offenen Punkte blockieren die reine Data-/Domain-Anbindung nicht. Vor UI-Verhalten, Bearbeitung oder Navigation den Backend-Vertrag entsprechend präzisieren bzw. erweitern. Eine spätere eigenständige Upcoming-Feature-Library nutzt `core:tasks`; ihre Routen und Navigation liegen in `app`. Keine Abhängigkeit auf `features:tasklist` einführen.

## Umsetzungsreihenfolge

1. Wire-Generierung, neues Domain-Modell und Repository-Signatur ergänzen.
2. Remote-Vertrag, RPC-Aufruf, Mapper und Repository-Methode umsetzen.
3. Betroffene Test-Doubles mitziehen und die gezielten Tests ergänzen.
4. Architektur-, JVM- und Plattformprüfungen ausführen.
5. Backend-Semantik vor dem separaten UI-Vorhaben klären.

## Prüfergebnisse der Umsetzung

- `verifyArchitecture`: erfolgreich, 18 Projekte und 233 Produktionsdateien geprüft.
- Gezielter JVM-Lauf für `core:tasks`, `features:tasklist` und `app`: 188 Tests ohne Fehler, darunter 14 neue Tests für Mapper, Repository und RPC-Adapter.
- Root `jvmTest`: erfolgreich; die JVM-Berichte enthalten insgesamt 447 Tests ohne Fehler.
- `:core:tasks:compileKotlinJs` und `:core:tasks:compileKotlinWasmJs`: erfolgreich.
- `:apps:android:assembleDebug`: erfolgreich.
- `git diff --check`: erfolgreich.

Beim vollständigen JVM-Lauf fiel ein bestehender Zufallstest in `HeaderRedactorPropertyTest` auf: Vertraulicher und öffentlicher Header erhielten denselben Wert, obwohl der Test gleichzeitig die Entfernung und unveränderte Beibehaltung dieses Werts verlangte. Die Generatoren verwenden jetzt unterschiedliche Präfixe. Nach dieser ausschließlichen Testdatenkorrektur lief die gesamte Prüfung erfolgreich durch.

Die Prüfungen verwenden lokale Test-Doubles; ein Live-Aufruf gegen das Backend wurde nicht durchgeführt. Die genannten offenen Backend-Fragen und die zukünftige UI bleiben unverändert offen.
