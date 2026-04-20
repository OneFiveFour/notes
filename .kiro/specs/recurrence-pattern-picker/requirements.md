# Requirements Document

## Introduction

This feature adds a recurrence pattern picker to the existing `TaskDateBottomSheet` in EchoList. Currently, the bottom sheet only contains a Material 3 `DatePicker` for selecting a due date. This feature extends it with a multi-selection pill composable for choosing a recurrence interval (Off, Daily, Weekly, Monthly, Yearly) and interval-specific configuration UI. The selected date always serves as the base date from which the next occurrence is calculated. When recurrence is disabled ("Off"), the selected date is the due date. This spec covers the UI layer only — backend wiring is out of scope.

## Glossary

- **TaskDateBottomSheet**: The existing `ModalBottomSheet` composable that opens when a user taps the date icon on a `MainTaskCard`. Currently contains only a `DatePicker`.
- **RecurrenceIntervalPicker**: A new composable that renders a horizontal row of selectable pill-shaped segments, one per recurrence interval option.
- **RecurrenceInterval**: An enum representing the five recurrence options: Off, Daily, Weekly, Monthly, Yearly.
- **Short_Label**: A compact text label shown on an unselected pill segment (Off, D, W, M, Y).
- **Full_Label**: An expanded text label shown on the currently selected pill segment (Off, Daily, Weekly, Monthly, Yearly).
- **Base_Date**: The date selected in the `DatePicker`. When recurrence is active, this is the reference point for calculating the next occurrence. When recurrence is off, this is the due date.
- **Interval_Detail_UI**: The composable area below the RecurrenceIntervalPicker that adapts its content based on the selected RecurrenceInterval.
- **Weekday_Checkboxes**: A row of seven checkboxes (one per weekday) shown when the Daily interval is selected.
- **Week_Interval_Input**: A numeric input for specifying "every N weeks", shown when the Weekly interval is selected.
- **Month_Interval_Input**: A composite input for specifying "every N months on the Mth day", shown when the Monthly interval is selected.

## Requirements

### Requirement 1: RecurrenceIntervalPicker Composable

**User Story:** As a user, I want to see a horizontal row of pill-shaped options below the date picker, so that I can choose a recurrence interval for my task.

#### Acceptance Criteria

1. THE RecurrenceIntervalPicker SHALL render exactly five selectable segments in a single horizontal row in the order: Off, Daily, Weekly, Monthly, Yearly.
2. THE RecurrenceIntervalPicker SHALL display the Short_Label for each unselected segment and the Full_Label for the selected segment.
3. WHEN the user taps a segment, THE RecurrenceIntervalPicker SHALL select that segment, swap its Short_Label to its Full_Label, and swap the previously selected segment's Full_Label to its Short_Label.
4. THE RecurrenceIntervalPicker SHALL have exactly one segment selected at all times.
5. THE RecurrenceIntervalPicker SHALL default to the "Off" segment when no prior recurrence is set.
6. THE RecurrenceIntervalPicker SHALL be a stateless composable that receives the current selection and emits a callback when the user taps a different segment.
7. THE RecurrenceIntervalPicker SHALL use `EchoListTheme` tokens for all colors, typography, spacing, and shapes.

### Requirement 2: Label Definitions

**User Story:** As a user, I want to see compact labels on unselected options and full labels on the selected option, so that all options fit horizontally while the active choice remains readable.

#### Acceptance Criteria

1. THE RecurrenceInterval "Off" SHALL have a Short_Label of "Off" and a Full_Label of "Off".
2. THE RecurrenceInterval "Daily" SHALL have a Short_Label of "D" and a Full_Label of "Daily".
3. THE RecurrenceInterval "Weekly" SHALL have a Short_Label of "W" and a Full_Label of "Weekly".
4. THE RecurrenceInterval "Monthly" SHALL have a Short_Label of "M" and a Full_Label of "Monthly".
5. THE RecurrenceInterval "Yearly" SHALL have a Short_Label of "Y" and a Full_Label of "Yearly".

### Requirement 3: Integration into TaskDateBottomSheet

**User Story:** As a user, I want the recurrence picker to appear below the date picker in the existing bottom sheet, so that I can set both a base date and a recurrence pattern in one place.

#### Acceptance Criteria

1. THE TaskDateBottomSheet SHALL display the RecurrenceIntervalPicker below the existing DatePicker.
2. THE TaskDateBottomSheet SHALL display the Interval_Detail_UI below the RecurrenceIntervalPicker.
3. WHILE the "Off" interval is selected, THE TaskDateBottomSheet SHALL treat the selected date as the due date.
4. WHILE a recurrence interval other than "Off" is selected, THE TaskDateBottomSheet SHALL treat the selected date as the Base_Date for calculating the next occurrence.

### Requirement 4: Interval Detail UI — Off

**User Story:** As a user, when I select "Off", I want no additional configuration UI, so that the interface stays clean for a simple due date.

#### Acceptance Criteria

1. WHILE the "Off" interval is selected, THE Interval_Detail_UI SHALL display no additional content.

### Requirement 5: Interval Detail UI — Daily

**User Story:** As a user, when I select "Daily", I want to see checkboxes for each weekday, so that I can specify which days the task recurs on.

#### Acceptance Criteria

1. WHILE the "Daily" interval is selected, THE Interval_Detail_UI SHALL display exactly seven checkboxes, one for each day of the week.
2. THE Weekday_Checkboxes SHALL be labeled with the name or abbreviation of each weekday (Monday through Sunday).
3. WHEN the user toggles a Weekday_Checkbox, THE Interval_Detail_UI SHALL update the checked state of that checkbox.
4. THE Weekday_Checkboxes composable SHALL be stateless, receiving the current checked states and emitting a callback when a checkbox is toggled.

### Requirement 6: Interval Detail UI — Weekly

**User Story:** As a user, when I select "Weekly", I want to specify how many weeks between recurrences, so that I can set biweekly or other multi-week patterns.

#### Acceptance Criteria

1. WHILE the "Weekly" interval is selected, THE Interval_Detail_UI SHALL display a labeled numeric input for specifying the week interval.
2. THE Week_Interval_Input SHALL display the text "Every [n] week(s)" where [n] is the user-entered value.
3. THE Week_Interval_Input SHALL accept only positive integer values.
4. THE Week_Interval_Input SHALL default to 1.

### Requirement 7: Interval Detail UI — Monthly

**User Story:** As a user, when I select "Monthly", I want to specify how many months between recurrences and on which day of the month, so that I can set patterns like "every 2 months on the 15th".

#### Acceptance Criteria

1. WHILE the "Monthly" interval is selected, THE Interval_Detail_UI SHALL display labeled numeric inputs for specifying the month interval and the day of the month.
2. THE Month_Interval_Input SHALL display the text "Every [n] month(s) on the [m]th day" where [n] is the month interval and [m] is the day of the month.
3. THE Month_Interval_Input SHALL accept only positive integer values for the month interval.
4. THE Month_Interval_Input SHALL accept day-of-month values between 1 and 31 inclusive.
5. THE Month_Interval_Input SHALL default the month interval to 1 and the day of the month to the day component of the currently selected Base_Date.

### Requirement 8: Interval Detail UI — Yearly

**User Story:** As a user, when I select "Yearly", I want no additional configuration UI, so that the yearly recurrence simply uses the selected date.

#### Acceptance Criteria

1. WHILE the "Yearly" interval is selected, THE Interval_Detail_UI SHALL display no additional content.

### Requirement 9: UI-Only Scope

**User Story:** As a developer, I want the recurrence picker to manage its own UI state without persisting to the backend, so that the feature can be wired up in a later iteration.

#### Acceptance Criteria

1. THE RecurrenceIntervalPicker and Interval_Detail_UI SHALL manage recurrence state locally within the TaskDateBottomSheet composable scope.
2. THE TaskDateBottomSheet SHALL expose recurrence selection data through callbacks to its parent, matching the existing callback pattern used for `onDueDateSelected`.
3. THE RecurrenceIntervalPicker and all Interval_Detail_UI composables SHALL follow the stateless composable pattern used throughout the EchoList codebase.
