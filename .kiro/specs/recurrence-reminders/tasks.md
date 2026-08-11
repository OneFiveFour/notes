# Implementation Plan: Recurrence Reminders

## Overview

This plan implements two capabilities for the EchoList task list editor: (1) visual urgency coloring on the DueDateTag pill based on proximity to due date, and (2) cross-platform local notifications that fire when a recurring task becomes due. The implementation proceeds from pure domain logic through UI integration to platform-specific notification scheduling, with property-based tests validating correctness properties throughout.

## Tasks

- [x] 1. Implement domain layer urgency computation
  - [x] 1.1 Create DueDateUrgency enum and DueDateUrgencyCalculator object
    - Create `DueDateUrgency` enum with `Normal`, `Warning`, `Overdue` values in `commonMain/domain/`
    - Implement `DueDateUrgencyCalculator.computeUrgency(dueDate: LocalDate, today: LocalDate): DueDateUrgency`
    - Use `kotlinx-datetime` `LocalDate.toEpochDays()` for day difference calculation
    - Return `Overdue` when difference ≤ 0, `Warning` when 1–3, `Normal` when > 3
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 1.2 Write property tests for DueDateUrgencyCalculator
    - **Property 1: Urgency partitioning is exhaustive and correct**
    - **Property 2: Urgency monotonicity**
    - **Property 4: Invalid date defaults to Normal (at call-site level)**
    - Use Kotest Property with `Arb.localDate()` generators
    - Verify exhaustive partition: for any (dueDate, today), exactly one enum value is returned
    - Verify monotonicity: for dueDate1 < dueDate2, urgency(dueDate1) ≥ urgency(dueDate2) in severity
    - **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2**

- [ ] 2. Implement urgency color resolution and update DueDateTag composable
  - [~] 2.1 Create resolveUrgencyColors composable helper
    - Create `resolveUrgencyColors(urgency: DueDateUrgency): Pair<Color, Color>` in `commonMain/ui/` near DueDateTag
    - Map `Normal` → `(EchoListTheme.materialColors.surfaceVariant, EchoListTheme.materialColors.onSurfaceVariant)`
    - Map `Warning` → `(EchoListTheme.echoListColorScheme.warning, EchoListTheme.echoListColorScheme.onWarning)`
    - Map `Overdue` → `(EchoListTheme.materialColors.error, EchoListTheme.materialColors.onError)`
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [~] 2.2 Update DueDateTag composable to accept and render urgency
    - Add `urgency: DueDateUrgency` parameter to `DueDateTag`
    - Call `resolveUrgencyColors(urgency)` to obtain background and text colors
    - Apply background color to the pill `Surface` and text color to `Text` and recurrence `Icon` tint
    - Ensure DueDateTag remains stateless — urgency is computed externally
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [~] 2.3 Update MainTaskEditorCard to compute urgency and pass to DueDateTag
    - Import `Clock.System.todayIn(TimeZone.currentSystemDefault())` for current date
    - Use `remember` keyed on dueDate text and today to compute urgency
    - Handle unparseable/empty dates by defaulting to `DueDateUrgency.Normal`
    - Pass computed urgency to the `DueDateTag` composable call
    - _Requirements: 1.1, 1.2, 1.3, 3.1, 3.2_

  - [~] 2.4 Write property test for color resolution correctness
    - **Property 3: Color resolution correctness**
    - For each `DueDateUrgency` value, verify the returned color pair matches the specification
    - **Validates: Requirements 2.1, 2.2, 2.3**

- [~] 3. Checkpoint - Urgency coloring complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Implement NotificationScheduler interface and scheduling logic
  - [~] 4.1 Create NotificationScheduler interface in domain layer
    - Define `NotificationScheduler` interface in `commonMain/domain/` with `schedule()` and `cancel()` suspend functions
    - `schedule(taskId: String, title: String, body: String, dueDateIso: String)`
    - `cancel(taskId: String)`
    - _Requirements: 4.1, 4.2, 4.5_

  - [~] 4.2 Create TaskNotificationData model and scheduleTaskNotification helper
    - Define `TaskNotificationData` data class with `taskId`, `taskDescription`, `taskListName`, `dueDateIso`
    - Implement `scheduleTaskNotification(scheduler, task, taskListName)` suspend function
    - If task has blank dueDate or blank recurrence → call `cancel(task.id)`
    - Otherwise → call `schedule()` with title "Task due: {taskListName}", body = description (or taskListName if empty), truncated to 200 chars
    - Skip scheduling if dueDate is strictly before today
    - _Requirements: 4.1, 4.3, 5.1, 5.2, 5.3, 5.5, 8.1, 8.2_

  - [~] 4.3 Write unit tests for notification scheduling logic
    - Use a fake `NotificationScheduler` implementation to capture calls
    - Verify `schedule` is called when task has valid dueDate + recurrence
    - Verify `cancel` is called when recurrence is empty
    - Verify `cancel` is called when dueDate is empty
    - Verify past dates skip scheduling
    - Verify today's date proceeds with scheduling
    - Verify body truncation at 200 characters
    - Verify empty description falls back to task list name
    - **Property 5: Schedule and cancel correctness**
    - **Property 7: Notification content format**
    - **Property 8: Start-of-day scheduling**
    - **Property 10: Past date skips scheduling**
    - **Validates: Requirements 4.1, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 5.5, 8.1, 8.2**

- [ ] 5. Implement platform-specific NotificationScheduler implementations
  - [~] 5.1 Implement AndroidNotificationScheduler
    - Use `AlarmManager.setExactAndAllowWhileIdle()` with `RTC_WAKEUP` for scheduling
    - Create `TaskReminderReceiver` BroadcastReceiver to post notifications via `NotificationManager`
    - Use `PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE` for idempotent replacement
    - Set notification visibility to `VISIBILITY_PRIVATE` with redacted lock screen placeholder
    - Convert ISO date to epoch millis at 00:00 in user's local time zone
    - Handle permission denied as no-op with warning log
    - Skip scheduling if due date is before today
    - Cancel: remove PendingIntent and dismiss displayed notification
    - _Requirements: 4.2, 5.4, 6.1, 6.2, 7.1, 7.3, 8.1, 9.1, 9.2_

  - [~] 5.2 Implement IosNotificationScheduler
    - Use `UNUserNotificationCenter` with `UNCalendarNotificationTrigger` at midnight local time
    - Set `hiddenPreviewsBodyPlaceholder` on notification category for lock screen privacy
    - Use task ID as notification request identifier for idempotent replacement
    - Cancel via `removePendingNotificationRequests(withIdentifiers:)`
    - Handle permission denied as no-op with warning log
    - Skip scheduling if due date is before today
    - _Requirements: 5.4, 6.3, 7.1, 7.3, 8.1, 9.3_

  - [~] 5.3 Implement JvmNotificationScheduler
    - Check `SystemTray.isSupported` — use system tray notification if available
    - Fall back to coroutine-delayed in-process alert if SystemTray is not supported
    - Schedule notification at midnight local time using `kotlinx-datetime` conversion
    - Handle permission denied / unsupported as no-op with warning log
    - Skip scheduling if due date is before today
    - _Requirements: 5.4, 6.4, 7.1, 8.1_

  - [~] 5.4 Implement WebNotificationScheduler (JS and WasmJS)
    - Use browser `Notification` API for both JS and WasmJS targets
    - Use `setTimeout` or `window.setTimeout` for delayed firing at midnight local time
    - Handle permission denied as no-op with warning log
    - Skip scheduling if due date is before today
    - _Requirements: 5.4, 6.5, 7.1, 8.1_

- [ ] 6. Wire DI bindings and ViewModel integration
  - [~] 6.1 Add NotificationScheduler Koin bindings per platform
    - Bind `NotificationScheduler` interface in each platform's Koin module (`androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`)
    - Use `single<NotificationScheduler> { PlatformNotificationScheduler(...) }` pattern
    - _Requirements: 6.6_

  - [~] 6.2 Integrate notification scheduling in EditTaskListViewModel
    - Inject `NotificationScheduler` via Koin into `EditTaskListViewModel`
    - Call `scheduleTaskNotification()` when a task with recurrence is saved/updated
    - Call `scheduler.cancel(taskId)` when a task is deleted
    - Launch scheduling on `viewModelScope` with `Dispatchers.Default`
    - _Requirements: 4.1, 4.3, 4.4_

  - [~] 6.3 Write unit tests for ViewModel notification integration
    - Use fake `NotificationScheduler` to verify scheduling/cancellation calls
    - Verify saving task with recurrence triggers `schedule`
    - Verify deleting task triggers `cancel`
    - Verify removing recurrence triggers `cancel`
    - **Property 6: Notification idempotency**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4**

- [ ] 7. Checkpoint - Full feature integration complete
  - Ensure all tests pass, ask the user if questions arise.

  - [~] 7.1 Write property tests for notification idempotency and permission handling
    - **Property 6: Notification idempotency** — scheduling twice results in exactly one pending notification
    - **Property 9: Permission denied is a no-op** — verify no exception, no scheduling when permission denied
    - Use fake scheduler with state tracking to verify single-notification invariant
    - **Validates: Requirements 4.2, 7.1, 7.3**

- [~] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The design uses Kotlin with kotlinx-datetime — no language choice needed
- Platform implementations (task 5) can be developed in parallel across platforms
- The urgency coloring (tasks 1–2) is independent of the notification system (tasks 4–6) and can ship separately

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "4.1"] },
    { "id": 1, "tasks": ["1.2", "2.1", "4.2"] },
    { "id": 2, "tasks": ["2.2", "2.3", "4.3"] },
    { "id": 3, "tasks": ["2.4", "5.1", "5.2", "5.3", "5.4"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["6.2"] },
    { "id": 6, "tasks": ["6.3", "7.1"] }
  ]
}
```
