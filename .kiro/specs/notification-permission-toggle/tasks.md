# Implementation Plan: Notification Permission Toggle

## Overview

This plan implements the "Notification Permission Toggle" feature for EchoList. The feature adds a per-task notification toggle to the MainTaskSettingsScreen, integrates platform-specific permission checking/requesting, and ensures the existing scheduling logic respects the new `isNotificationEnabled` flag.

**Key design decision:** The toggle controls user intent (`isNotificationEnabled`). Permission checking happens **only once** — when the user leaves the screen via `onScreenLeaving()` (triggered by `DisposableEffect.onDispose`). The existing `confirm()` method emits immediately on every change without any permission logic.

Implementation proceeds bottom-up: domain model changes → data layer extensions → ViewModel logic → UI composables → navigation wiring → platform implementations → testing.

## Tasks

- [x] 1. Domain Layer — Model & Scheduling Modifications
  - [x] 1.1 Add `isNotificationEnabled` field to `MainTask` data class
    - Add `val isNotificationEnabled: Boolean = true` parameter to `MainTask` in `commonMain/.../domain/model/MainTask.kt`
    - Default value `true` ensures backward compatibility with existing tasks
    - _Requirements: 2.1, 2.2_

  - [x] 1.2 Add early-return guard in `scheduleTaskNotification`
    - In `commonMain/.../domain/NotificationScheduling.kt`, add a check at the top of the function: if `!task.isNotificationEnabled`, call `scheduler.cancel(task.id)` and return immediately
    - All existing logic below the guard remains unchanged
    - _Requirements: 5.1, 5.2_

  - [x] 1.3 Create `NotificationPermissionChecker` interface
    - Create `commonMain/.../domain/NotificationPermissionChecker.kt`
    - Define `interface NotificationPermissionChecker` with `suspend fun isGranted(): Boolean`
    - _Requirements: 6.1_

  - [x] 1.4 Create `NotificationPermissionRequester` interface
    - Create `commonMain/.../domain/NotificationPermissionRequester.kt`
    - Define `interface NotificationPermissionRequester` with `suspend fun request(): Boolean`
    - _Requirements: 6.2_

  - [x] 1.5 Write property tests for `scheduleTaskNotification` with `isNotificationEnabled=false`
    - **Property 5: isNotificationEnabled=false → immer cancel**
    - Generate random MainTask instances with `isNotificationEnabled = false` and varying dueDate/recurrence values
    - Assert `scheduler.cancel(task.id)` is always called and `scheduler.schedule(...)` is never called
    - Minimum 100 iterations
    - Tag: `Feature: notification-permission-toggle, Property 5: isNotificationEnabled=false → immer cancel`
    - **Validates: Requirements 5.1**

- [x] 2. Data Layer — Model Extensions
  - [x] 2.1 Add `isNotificationEnabled` field to `UiMainTask`
    - Add `isNotificationEnabled: Boolean = true` constructor parameter
    - Add `var isNotificationEnabled by mutableStateOf(isNotificationEnabled)` property
    - Update `toDomain()` to include `isNotificationEnabled` in the returned `MainTask`
    - Update `fromDomain()` to read `domain.isNotificationEnabled`
    - _Requirements: 2.1, 2.3_

  - [x] 2.2 Add `isNotificationEnabled` to `MainTaskSettingsResult`
    - Add `val isNotificationEnabled: Boolean` field to the data class
    - _Requirements: 2.3_

  - [x] 2.3 Add `currentIsNotificationEnabled` to `MainTaskSettingsRoute`
    - Add `val currentIsNotificationEnabled: Boolean = true` parameter to the `@Serializable` data class
    - _Requirements: 2.4_

  - [x] 2.4 Ensure `isNotificationEnabled` is preserved locally during sync
    - The `isNotificationEnabled` field is purely on-device — it is NOT sent to the backend
    - In `TaskListMapper.toDomain(tasks.v1.MainTask)`: the proto has no `notifications_enabled` field, so the domain default (`true`) applies for tasks loaded from the server
    - Ensure `UiMainTask.isNotificationEnabled` survives a sync round-trip: when the server returns a task, `fromDomain()` uses the default `true`, so existing local toggle state must be preserved in the ViewModel rather than overwritten by server response
    - _Requirements: 2.1_

  - [x] 2.5 Write property test for route serialization round-trip
    - **Property 6: Route-Parameter Round-Trip**
    - Generate random Boolean values for `currentIsNotificationEnabled`
    - Serialize and deserialize `MainTaskSettingsRoute`, assert `currentIsNotificationEnabled` is preserved
    - Minimum 100 iterations
    - Tag: `Feature: notification-permission-toggle, Property 6: Route-Parameter Round-Trip`
    - **Validates: Requirements 2.4**

- [x] 3. Checkpoint — Domain & Data Layer
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. UI Layer — MainTaskSettingsViewModel Permission Logic
  - [x] 4.1 Add new constructor parameters to `MainTaskSettingsViewModel`
    - Add `currentIsNotificationEnabled: Boolean`, `permissionChecker: NotificationPermissionChecker`, `permissionRequester: NotificationPermissionRequester`
    - Initialize UI state with `isNotificationEnabled = currentIsNotificationEnabled` and compute `isNotificationToggleEnabled` from recurrence state
    - _Requirements: 2.4, 3.1_

  - [x] 4.2 Extend `MainTaskSettingsUiState.Ready` with notification fields
    - Add `val isNotificationEnabled: Boolean = true`
    - Add `val isNotificationToggleEnabled: Boolean = true`
    - _Requirements: 1.1, 1.4, 7.1_

  - [x] 4.3 Implement `onNotificationToggleChanged(enabled: Boolean)`
    - Update `isNotificationEnabled` in the Ready state via `updateReady`
    - Call `confirm()` to emit result immediately (no permission logic here)
    - _Requirements: 1.3_

  - [x] 4.4 Modify `onRecurrenceIntervalSelected` for auto-disable
    - When interval is `RecurrenceInterval.Off`: set `isNotificationEnabled = false` and `isNotificationToggleEnabled = false`
    - When interval is not Off: set `isNotificationToggleEnabled = true`, preserve current `isNotificationEnabled`
    - _Requirements: 1.4, 1.5_

  - [x] 4.5 Modify `confirm()` to include `isNotificationEnabled` in emitted result
    - Add `isNotificationEnabled = currentState.isNotificationEnabled` to the `MainTaskSettingsResult` constructor call inside `confirm()`
    - `confirm()` remains synchronous and contains NO permission checking logic — it emits immediately on every change
    - _Requirements: 2.3_

  - [x] 4.6 Implement `onScreenLeaving()` with permission check/request logic
    - Add new public method `fun onScreenLeaving()` that launches a coroutine in `viewModelScope`
    - Logic:
      1. Read current state; if not Ready, return
      2. If `!isNotificationEnabled` or `recurrenceState == RecurrenceState.Off` → return (no permission check needed)
      3. Call `runCatching { permissionChecker.isGranted() }.getOrDefault(false)` — if true, return
      4. Call `runCatching { permissionRequester.request() }.getOrDefault(false)` — if true, return
      5. If permission denied: update state with `isNotificationEnabled = false`, then re-emit corrected result via `resultBus.emit(...)` with `isNotificationEnabled = false`
    - This is the ONLY place where permission checking occurs
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3_

  - [x] 4.7 Write property tests for ViewModel (Properties 1–4)
    - **Property 1: Toggle-Konsistenz** — For all Boolean `enabled` values, `onNotificationToggleChanged(enabled)` results in emitted `MainTaskSettingsResult.isNotificationEnabled` matching the enabled value (immediate emission via confirm(), no permission involved)
    - **Property 2: Recurrence.Off erzwingt Deaktivierung** — For all active RecurrenceState values, switching to Off sets `isNotificationEnabled=false` and `isNotificationToggleEnabled=false`
    - **Property 3: Berechtigungsprüfungs-Guard (onScreenLeaving)** — For all `(isNotificationEnabled, recurrenceState)` combinations, when `onScreenLeaving()` is called: permissionChecker is only called when `isNotificationEnabled == true` AND `recurrenceState != Off`
    - **Property 4: Berechtigungsverweigerung erzwingt Deaktivierung (onScreenLeaving)** — For all states with `isNotificationEnabled=true` and active recurrence where permission is denied, `onScreenLeaving()` re-emits with `isNotificationEnabled = false`
    - Minimum 100 iterations per property
    - Mock `NotificationPermissionChecker`, `NotificationPermissionRequester`, `MainTaskSettingsResultBus`
    - Tags: `Feature: notification-permission-toggle, Property {N}: {Title}`
    - **Validates: Requirements 1.3, 1.4, 1.5, 2.3, 3.1, 3.4, 4.2, 4.3**

- [x] 5. UI Layer — MainTaskSettingsScreen Toggle
  - [x] 5.1 Add `onNotificationToggleChanged` callback parameter to `MainTaskSettingsScreen`
    - Add `onNotificationToggleChanged: (Boolean) -> Unit` to both the outer `MainTaskSettingsScreen` and inner `MainTaskSettingsContent` composables
    - _Requirements: 1.3, 7.1_

  - [x] 5.2 Add "Notifications" `SettingsSection` with Material 3 Switch
    - Place below the "Repeat" section
    - Use `Switch` composable bound to `uiState.isNotificationEnabled` with `enabled = uiState.isNotificationToggleEnabled`
    - Label text "Erinnern" with `EchoListTheme.typography.bodyMedium`
    - Use `EchoListTheme.materialColors`, `EchoListTheme.dimensions` for all styling
    - _Requirements: 7.1, 7.2, 7.4_

  - [x] 5.3 Add helper text when toggle is disabled
    - Use `AnimatedVisibility(visible = !uiState.isNotificationToggleEnabled)`
    - Display explanatory text: "Notifications sind nur bei aktiver Wiederholung verfügbar."
    - Use `EchoListTheme.typography.bodySmall` and `EchoListTheme.materialColors.onSurfaceVariant`
    - _Requirements: 7.3_

  - [x] 5.4 Wire the new callback in `MainTaskSettingsContent`
    - Pass `onNotificationToggleChanged` through to the Switch `onCheckedChange`
    - _Requirements: 1.3_

- [ ] 6. Integration — EditTaskListViewModel & Navigation Wiring
  - [~] 6.1 Update `EditTaskListViewModel` settingsResultBus collector
    - In the `collect` block, also update `task.isNotificationEnabled = result.isNotificationEnabled`
    - This ensures the new flag is synced when the settings screen emits results
    - _Requirements: 5.3_

  - [~] 6.2 Update `onNavigateToSettings` callback to pass `isNotificationEnabled`
    - In `App.kt` inside `entry<EditTaskListRoute>`, change `onNavigateToSettings` lambda signature to accept a 4th parameter: `currentIsNotificationEnabled: Boolean`
    - Pass `currentIsNotificationEnabled` to `MainTaskSettingsRoute` constructor
    - In `EditTaskListScreen`, pass `mainTask.isNotificationEnabled` as the 4th argument when invoking `onNavigateToSettings`
    - _Requirements: 2.4_

  - [~] 6.3 Update Koin binding for `MainTaskSettingsViewModel` in `AppModules.kt`
    - Add `currentIsNotificationEnabled = params.get()` and inject `permissionChecker = get()` and `permissionRequester = get()`
    - _Requirements: 6.3_

  - [~] 6.4 Update `entry<MainTaskSettingsRoute>` in `App.kt` with DisposableEffect and new params
    - Pass `route.currentIsNotificationEnabled` as 4th param to ViewModel via `parametersOf`
    - Add `DisposableEffect(viewModel) { onDispose { viewModel.onScreenLeaving() } }` to trigger permission check on back-navigation
    - Pass `onNotificationToggleChanged = viewModel::onNotificationToggleChanged` to `MainTaskSettingsScreen`
    - _Requirements: 2.4, 3.1, 7.1_

  - [~] 6.5 Write integration tests for toggle deactivation → sync → cancel flow
    - Test that when toggle changes from enabled to disabled, `EditTaskListViewModel` triggers `requestSync()` and the scheduler cancels the notification
    - _Requirements: 5.3_

- [ ] 7. Platform Implementations
  - [~] 7.1 Android: Implement `AndroidNotificationPermissionChecker` and `AndroidNotificationPermissionRequester`
    - Checker: use `ContextCompat.checkSelfPermission(POST_NOTIFICATIONS)` on API 33+, return `true` below
    - Requester: use `PermissionResultBridge` pattern with `CompletableDeferred` for Activity result integration
    - Place in `androidMain/.../domain/` or `androidMain/.../data/`
    - _Requirements: 6.4_

  - [~] 7.2 iOS: Implement `IosNotificationPermissionChecker` and `IosNotificationPermissionRequester`
    - Checker: query `UNUserNotificationCenter.current().getNotificationSettings()`, check `.authorizationStatus == .authorized`
    - Requester: call `UNUserNotificationCenter.requestAuthorization(options: [.alert, .sound])`
    - Place in `iosMain/.../domain/` or `iosMain/.../data/`
    - _Requirements: 6.5_

  - [~] 7.3 JVM: Implement `JvmNotificationPermissionChecker` and `JvmNotificationPermissionRequester`
    - Both methods always return `true` (desktop requires no explicit permission)
    - Place in `jvmMain/.../domain/` or `jvmMain/.../data/`
    - _Requirements: 6.6_

  - [~] 7.4 JS: Implement `JsNotificationPermissionChecker` and `JsNotificationPermissionRequester`
    - Checker: check `Notification.permission == "granted"`
    - Requester: call `Notification.requestPermission()` and return `result == "granted"`
    - Place in `jsMain/.../domain/` or `jsMain/.../data/`
    - _Requirements: 6.7_

  - [~] 7.5 WasmJS: Implement `WasmJsNotificationPermissionChecker` and `WasmJsNotificationPermissionRequester`
    - Same logic as JS via `JsFun` interop
    - Place in `wasmJsMain/.../domain/` or `wasmJsMain/.../data/`
    - _Requirements: 6.7_

  - [~] 7.6 Add Koin bindings for all platforms
    - Create or extend `NotificationModule` in each platform source set
    - Bind `NotificationPermissionChecker` and `NotificationPermissionRequester` to platform implementations
    - Register modules in `appModules` list
    - _Requirements: 6.3_

- [~] 8. Final Checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (minimum 100 iterations each)
- Unit tests validate specific examples and edge cases
- The design document uses Kotlin (Compose Multiplatform), so no language selection is needed
- Platform implementations (Task 7) can largely be done in parallel across platforms
- The `isNotificationEnabled` default of `true` ensures backward compatibility with existing tasks that have no stored value
- `confirm()` is purely synchronous and never checks permissions — it emits the current state immediately
- Permission logic lives exclusively in `onScreenLeaving()`, triggered by `DisposableEffect.onDispose` in the navigation entry

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.3", "1.4"] },
    { "id": 1, "tasks": ["1.2", "2.1", "2.2", "2.3"] },
    { "id": 2, "tasks": ["1.5", "2.4", "2.5", "4.1", "4.2"] },
    { "id": 3, "tasks": ["4.3", "4.4", "4.5", "4.6"] },
    { "id": 4, "tasks": ["4.7", "5.1", "5.2", "5.3", "5.4"] },
    { "id": 5, "tasks": ["6.1", "6.2", "6.3", "6.4", "7.1", "7.2", "7.3", "7.4", "7.5"] },
    { "id": 6, "tasks": ["6.5", "7.6"] }
  ]
}
```
