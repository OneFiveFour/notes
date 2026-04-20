# Implementation Plan: Recurrence Pattern Picker

## Overview

This plan implements a recurrence pattern picker UI within the existing `TaskDateBottomSheet` in the `ui/edittasklist` package. The approach is bottom-up: define data models first, build leaf composables, then wire everything into the bottom sheet. All composables are stateless (receive state, emit callbacks). All styling uses `EchoListTheme` tokens. No backend persistence — UI-only scope.

## Tasks

- [x] 1. Define RecurrenceInterval and RecurrenceState data models
  - [x] 1.1 Create `RecurrenceInterval` sealed interface with Off, Daily, Weekly, Monthly, Yearly data objects
    - Each variant carries `shortLabel` and `fullLabel` properties
    - Add a `companion object` with an `entries` list in the specified order: Off, Daily, Weekly, Monthly, Yearly
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrenceInterval.kt`
    - _Requirements: 1.1, 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 1.2 Create `RecurrenceState` sealed interface with Off, Daily, Weekly, Monthly, Yearly variants
    - Each variant holds its own configuration data (e.g., `Daily.selectedDays: Set<DayOfWeek>`, `Weekly.everyNWeeks: Int`, `Monthly.everyNMonths: Int` + `dayOfMonth: Int`)
    - Each variant exposes an `interval: RecurrenceInterval` property
    - Defaults: `Daily.selectedDays = emptySet()`, `Weekly.everyNWeeks = 1`, `Monthly.everyNMonths = 1`, `Monthly.dayOfMonth = 1`
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrenceState.kt`
    - _Requirements: 1.4, 1.5, 5.4, 6.4, 7.5, 9.1_

  - [x] 1.3 Write property test: Label mapping correctness (Property 1)
    - **Property 1: Label mapping correctness**
    - For every `RecurrenceInterval` in `entries`, verify `shortLabel` and `fullLabel` match the spec: Off→("Off","Off"), Daily→("D","Daily"), Weekly→("W","Weekly"), Monthly→("M","Monthly"), Yearly→("Y","Yearly")
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

  - [x] 1.4 Write property test: Selected vs unselected label display (Property 2)
    - **Property 2: Selected vs unselected label display**
    - For any `RecurrenceInterval` chosen as selected, a label function returns `fullLabel` for that interval and `shortLabel` for every other interval
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 1.2, 1.3**

  - [x] 1.5 Write unit tests for RecurrenceInterval and RecurrenceState
    - Verify `RecurrenceInterval.entries` has exactly 5 elements in order: Off, Daily, Weekly, Monthly, Yearly
    - Verify default `RecurrenceState` is `Off`
    - Verify `RecurrenceState.Weekly` defaults `everyNWeeks` to 1
    - Verify `RecurrenceState.Monthly` defaults `everyNMonths` to 1
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternTest.kt`
    - _Requirements: 1.1, 1.4, 1.5, 6.4, 7.5_

- [x] 2. Implement validation helpers
  - [x] 2.1 Create `RecurrenceValidation.kt` with positive-integer and day-of-month validators
    - `isValidPositiveInt(input: String): Boolean` — true iff input parses to an integer ≥ 1
    - `isValidDayOfMonth(input: String): Boolean` — true iff input parses to an integer in [1, 31]
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrenceValidation.kt`
    - _Requirements: 6.3, 7.3, 7.4_

  - [x] 2.2 Write property test: Positive integer validation (Property 7)
    - **Property 7: Positive integer validation**
    - For any string input, the validator accepts iff the string parses to an integer ≥ 1
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 6.3, 7.3**

  - [x] 2.3 Write property test: Day-of-month range validation (Property 8)
    - **Property 8: Day-of-month range validation**
    - For any integer input, the validator accepts iff the value is in [1, 31]
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 7.4**

- [x] 3. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement RecurrenceIntervalPicker composable
  - [x] 4.1 Create `RecurrenceIntervalPicker` composable
    - Stateless composable: receives `selectedInterval: RecurrenceInterval` and emits `onIntervalSelected: (RecurrenceInterval) -> Unit`
    - Renders a horizontal `Row` of five pill-shaped `Surface` segments
    - Selected segment: `EchoListTheme.materialColors.primary` background, `onPrimary` text, shows `fullLabel`
    - Unselected segments: `EchoListTheme.materialColors.surfaceVariant` background, `onSurfaceVariant` text, shows `shortLabel`
    - Uses `EchoListTheme.shapes.small` for pill rounding, `EchoListTheme.dimensions.xs` spacing, `EchoListTheme.dimensions.l` horizontal padding
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrenceIntervalPicker.kt`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.6, 1.7_

- [x] 5. Implement interval detail composables
  - [x] 5.1 Create `DailyDetailContent` composable
    - Stateless: receives `selectedDays: Set<DayOfWeek>`, emits `onDayToggled: (DayOfWeek, Boolean) -> Unit`
    - Renders a horizontal row of seven checkboxes labeled Mon–Sun
    - Uses `EchoListTheme.typography.labelSmall` for labels, `EchoListTheme.dimensions.xs` spacing
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/DailyDetailContent.kt`
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 5.2 Write property test: Weekday toggle symmetric set operation (Property 4)
    - **Property 4: Weekday toggle is a symmetric set operation**
    - For any `Set<DayOfWeek>` and any `DayOfWeek`, toggling produces a set where presence is flipped and size differs by exactly 1
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 5.3**

  - [x] 5.3 Create `WeeklyDetailContent` composable
    - Stateless: receives `everyNWeeks: Int`, emits `onWeekCountChanged: (Int) -> Unit`
    - Displays "Every [n] week(s)" with an inline numeric text field
    - Accepts only positive integers (minimum 1), uses validation from `RecurrenceValidation`
    - Uses `EchoListTheme.typography.bodyMedium` for label text
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/WeeklyDetailContent.kt`
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [x] 5.4 Write property test: Weekly format string correctness (Property 5)
    - **Property 5: Weekly format string correctness**
    - For any positive integer `n`, the display text equals `"Every $n week(s)"`
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 6.2**

  - [x] 5.5 Create `MonthlyDetailContent` composable
    - Stateless: receives `everyNMonths: Int` and `dayOfMonth: Int`, emits `onMonthIntervalChanged: (Int) -> Unit` and `onDayOfMonthChanged: (Int) -> Unit`
    - Displays "Every [n] month(s) on the [m]th day" with two inline numeric text fields
    - Month interval: positive integers (≥ 1). Day of month: integers 1–31. Uses validation from `RecurrenceValidation`
    - Uses `EchoListTheme.typography.bodyMedium` for label text
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/MonthlyDetailContent.kt`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 5.6 Write property test: Monthly format string correctness (Property 6)
    - **Property 6: Monthly format string correctness**
    - For any positive integer `n` and any integer `m` in [1, 31], the display text equals `"Every $n month(s) on the ${m}th day"`
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 7.2**

- [x] 6. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Integrate recurrence picker into TaskDateBottomSheet
  - [x] 7.1 Update `TaskDateBottomSheet` to manage `RecurrenceState` and render recurrence UI
    - Add `onRecurrenceChanged: (RecurrenceState) -> Unit` callback parameter to the composable signature
    - Add local `RecurrenceState` via `remember { mutableStateOf(RecurrenceState.Off) }`
    - Place `RecurrenceIntervalPicker` below the existing `DatePicker`
    - Place interval detail content (`DailyDetailContent`, `WeeklyDetailContent`, `MonthlyDetailContent`, or empty for Off/Yearly) below the picker using a `when` on the current `RecurrenceState`
    - When interval changes, reset `RecurrenceState` to defaults for the new interval (use base date day for `Monthly.dayOfMonth`)
    - Call `onRecurrenceChanged` whenever `RecurrenceState` changes
    - Update `TaskDateSheetState` if needed
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/TaskDateBottomSheet.kt`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 8.1, 9.1, 9.2, 9.3_

  - [x] 7.2 Write property test: Date semantics depend on recurrence state (Property 3)
    - **Property 3: Date semantics depend on recurrence state**
    - For any `RecurrenceState`, if state is `Off` the date is a due date; if non-Off the date is a base date
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternPropertyTest.kt`
    - **Validates: Requirements 3.3, 3.4**

  - [x] 7.3 Write unit tests for TaskDateBottomSheet integration
    - Verify Off and Yearly intervals render no detail content
    - Verify Daily interval renders exactly 7 checkboxes
    - Verify Weekly interval renders a numeric input
    - Verify Monthly interval renders two numeric inputs
    - File: `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/edittasklist/RecurrencePatternTest.kt`
    - _Requirements: 4.1, 5.1, 6.1, 7.1, 8.1_

- [x] 8. Wire recurrence callback into EditTaskListScreen
  - [x] 8.1 Update `EditTaskListScreen` to pass `onRecurrenceChanged` to `TaskDateBottomSheet`
    - Add `onRecurrenceChanged` callback to `EditTaskListScreen` parameters (or handle locally if no ViewModel wiring is needed yet)
    - Pass the callback through to `TaskDateBottomSheet` at the call site in `EditTaskListScreen`
    - Update the preview composable to include the new parameter
    - File: `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListScreen.kt`
    - _Requirements: 9.2, 9.3_

- [x] 9. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- All composables follow the stateless pattern: receive state, emit callbacks
- All styling uses `EchoListTheme` tokens — no magic `dp` literals
- Test files: property tests in `RecurrencePatternPropertyTest.kt`, unit tests in `RecurrencePatternTest.kt` (both in `commonTest/.../ui/edittasklist/`)
