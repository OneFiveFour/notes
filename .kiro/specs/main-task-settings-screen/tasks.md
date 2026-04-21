# Implementation Plan: Main Task Settings Screen

## Overview

Replace the `ModalBottomSheet`-based due date/recurrence UI with a dedicated full-screen settings screen. This involves creating a new navigation route, ViewModel, and screen composable in a new `ui.maintasksettings` package, relocating shared recurrence components to `ui.recurrence`, wiring up result passing via a Koin-scoped `SharedFlow`, and removing the old bottom-sheet code from `EditTaskListScreen`.

## Tasks

- [x] 1. Relocate recurrence components to shared package
  - [x] 1.1 Move recurrence types and composables to `ui.recurrence` package
    - Move `RecurrenceInterval.kt`, `RecurrenceState.kt`, `RecurrenceValidation.kt`, `RecurrenceIntervalPicker.kt`, `DailyDetailContent.kt`, `WeeklyDetailContent.kt`, `MonthlyDetailContent.kt` from `ui.edittasklist` to `ui.recurrence`
    - Update package declarations to `net.onefivefour.echolist.ui.recurrence`
    - Update all import references in `TaskDateBottomSheet.kt`, `EditTaskListViewModel.kt`, and any other files that reference these types
    - _Requirements: 2.1_

- [x] 2. Create navigation route and date conversion utilities
  - [x] 2.1 Add `MainTaskSettingsRoute` to `Routes.kt`
    - Add `@Serializable data class MainTaskSettingsRoute(val mainTaskId: Long, val currentDueDate: String, val currentRecurrence: String) : NavKey`
    - Register in `navKeySerializersModule` polymorphic block
    - _Requirements: 1.1, 1.2_
  - [x] 2.2 Create `DateConversions.kt` in `ui.maintasksettings` package
    - Move `dueDateToUtcMillis` and `utcMillisToDueDate` from `TaskDateBottomSheet.kt` to `ui.maintasksettings.DateConversions.kt`
    - Update references in `TaskDateBottomSheet.kt` to import from the new location
    - _Requirements: 3.1_
  - [x] 2.3 Write property test for date conversion round-trip
    - **Property 2: Date conversion round-trip**
    - Generator: Random `LocalDate` instances (2000-01-01 to 2099-12-31) formatted as `YYYY-MM-DD`
    - Assertion: `utcMillisToDueDate(dueDateToUtcMillis(dateString)!!) == dateString`
    - **Validates: Requirements 3.1, 4.3**

- [x] 3. Implement RRULE conversion functions
  - [x] 3.1 Create `RruleConversions.kt` in `ui.maintasksettings` package
    - Implement `RecurrenceState.toRrule(): String` extension function
    - Implement `rruleToRecurrenceState(rrule: String): RecurrenceState` function
    - Handle all `RecurrenceState` subtypes: Off → `""`, Daily → `FREQ=DAILY;BYDAY=...`, Weekly → `FREQ=WEEKLY;INTERVAL=N`, Monthly → `FREQ=MONTHLY;INTERVAL=N;BYMONTHDAY=D`, Yearly → `FREQ=YEARLY`
    - Handle malformed RRULE input by returning `RecurrenceState.Off`
    - _Requirements: 4.4, 4.5_
  - [x] 3.2 Write property test for RRULE round-trip
    - **Property 1: RRULE round-trip**
    - Generator: Random `RecurrenceState` instances (Off, Daily with random day sets, Weekly with week counts 1–52, Monthly with month counts 1–12 and day 1–31, Yearly)
    - Assertion: `rruleToRecurrenceState(state.toRrule()) == state`
    - **Validates: Requirements 4.5**

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement MainTaskSettingsViewModel
  - [x] 5.1 Create `MainTaskSettingsUiState.kt` and `MainTaskSettingsResult.kt` in `ui.maintasksettings`
    - Define `MainTaskSettingsUiState` data class with `selectedDueDate: String`, `recurrenceState: RecurrenceState`, `initialDateMillis: Long?`
    - Define `MainTaskSettingsResult` data class with `mainTaskId: Long`, `dueDate: String`, `recurrence: String`
    - _Requirements: 4.2, 4.5_
  - [x] 5.2 Create `MainTaskSettingsViewModel.kt` in `ui.maintasksettings`
    - Accept `mainTaskId: Long`, `initialDueDate: String`, `initialRecurrence: String` as constructor parameters
    - Initialize date state from `initialDueDate` using `dueDateToUtcMillis`
    - Initialize recurrence state from `initialRecurrence` using `rruleToRecurrenceState`
    - Expose `StateFlow<MainTaskSettingsUiState>`
    - Implement `onDateSelected(dateMillis: Long)` — updates due date, clears recurrence to Off
    - Implement `onRecurrenceIntervalSelected(interval: RecurrenceInterval)` — updates recurrence state, clears due date when interval ≠ Off
    - Implement `onRecurrenceDetailChanged(state: RecurrenceState)` for detail parameter updates
    - Implement `onConfirm()` — emits `MainTaskSettingsResult` to the Koin-scoped `MutableSharedFlow<MainTaskSettingsResult>`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2_
  - [x] 5.3 Write property test for mutual exclusion: selecting a date clears recurrence
    - **Property 3: Selecting a date clears recurrence**
    - Generator: Random non-Off `RecurrenceState` + random valid date string
    - Assertion: After `onDateSelected`, `recurrenceState == RecurrenceState.Off` and `selectedDueDate == date`
    - **Validates: Requirements 5.1**
  - [x] 5.4 Write property test for mutual exclusion: selecting a recurrence clears due date
    - **Property 4: Selecting a recurrence clears due date**
    - Generator: Random non-empty date string + random non-Off `RecurrenceInterval`
    - Assertion: After `onRecurrenceIntervalSelected`, `selectedDueDate == ""` and `recurrenceState.interval == interval`
    - **Validates: Requirements 5.2**

- [x] 6. Implement MainTaskSettingsScreen composable
  - [x] 6.1 Create `MainTaskSettingsScreen.kt` in `ui.maintasksettings`
    - Stateless composable receiving `MainTaskSettingsUiState` and callbacks
    - Display a top bar with a back/close icon button and a confirm action button
    - Display a Material 3 `DatePicker` initialized with `initialDateMillis`
    - Display `RecurrenceIntervalPicker` below the DatePicker (imported from `ui.recurrence`)
    - Conditionally display `DailyDetailContent`, `WeeklyDetailContent`, or `MonthlyDetailContent` based on selected recurrence interval
    - Wire callbacks: `onDateSelected`, `onRecurrenceIntervalSelected`, `onRecurrenceDetailChanged`, `onConfirm`, `onBack`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 7. Register ViewModel and result flow in Koin, wire navigation in App.kt
  - [x] 7.1 Update `AppModules.kt` with new DI registrations
    - Add `single { MutableSharedFlow<MainTaskSettingsResult>() }` to the appropriate module
    - Register `MainTaskSettingsViewModel` in `navigationModule` with parameterized injection accepting `mainTaskId`, `initialDueDate`, `initialRecurrence`
    - _Requirements: 2.2_
  - [x] 7.2 Add `entry<MainTaskSettingsRoute>` block in `App.kt`
    - Create `MainTaskSettingsViewModel` via `koinViewModel` with route params
    - Collect `uiState` as lifecycle-aware state
    - Wire `MainTaskSettingsScreen` composable with ViewModel callbacks
    - On confirm callback, pop the back stack (`backStack.removeLastOrNull()`)
    - On back/dismiss callback, pop the back stack without emitting a result
    - _Requirements: 1.3, 7.1_

- [x] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Update EditTaskListScreen to navigate to settings and consume results
  - [x] 9.1 Update `EditTaskListScreen.kt` to navigate instead of showing bottom sheet
    - Remove `activeDateSheet` state variable and `TaskDateSheetState` usage
    - Remove the `TaskDateBottomSheet` composable invocation
    - Replace `onOpenTaskDateSheet` callback with `onNavigateToSettings: (Long) -> Unit` parameter
    - Pass `onNavigateToSettings` through to `TaskListContentCard` and `MainTaskCard`
    - _Requirements: 6.1, 8.2_
  - [x] 9.2 Update `EditTaskListViewModel.kt` to consume `MainTaskSettingsResult`
    - Inject `MutableSharedFlow<MainTaskSettingsResult>` via Koin constructor parameter
    - Collect results in `init` block — apply returned `dueDate` and `recurrence` to the matching `UiMainTask` by `mainTaskId`
    - Remove `onDueDateSelected` method (replaced by result handling)
    - _Requirements: 7.2, 7.3_
  - [x] 9.3 Update `App.kt` `entry<EditTaskListRoute>` block
    - Pass `onNavigateToSettings` callback that adds `MainTaskSettingsRoute` to the back stack with the task's current `mainTaskId`, `dueDate`, and `recurrence` values
    - Remove `onDueDateSelected` wiring
    - _Requirements: 6.1_

- [x] 10. Remove old bottom sheet code
  - [x] 10.1 Delete `TaskDateBottomSheet.kt` and `TaskDateSheetState`
    - Delete `TaskDateBottomSheet.kt` from `ui.edittasklist` package
    - Verify no remaining references to `TaskDateBottomSheet`, `TaskDateSheetState`, or `ModalBottomSheet` for due date/recurrence editing in `EditTaskListScreen`
    - _Requirements: 8.1, 8.2_

- [x] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The design uses Kotlin throughout — all implementation tasks use Kotlin with Compose Multiplatform
