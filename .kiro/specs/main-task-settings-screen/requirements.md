# Requirements Document

## Introduction

Replace the current `ModalBottomSheet`-based due date and recurrence settings UI (`TaskDateBottomSheet.kt`) with a dedicated full-screen settings screen. The new screen is navigated to via the existing AndroidX Navigation 3 infrastructure, has its own ViewModel and package (`ui.maintasksettings`), and reuses the existing recurrence picker components. The `EditTaskListScreen` no longer manages bottom-sheet state; instead it triggers navigation to the new route. The new screen receives the current due date and recurrence values, lets the user edit them, and returns the result to the calling screen.

## Glossary

- **Settings_Screen**: The new full-screen Compose UI that displays a Material 3 DatePicker and recurrence interval picker for a single main task.
- **Settings_ViewModel**: The ViewModel that owns the due-date and recurrence state for the Settings_Screen.
- **Edit_Screen**: The existing `EditTaskListScreen` composable and its associated `EditTaskListViewModel`.
- **MainTaskSettingsRoute**: The new `@Serializable` navigation route data class for the Settings_Screen.
- **RecurrenceIntervalPicker**: The existing pill-style interval selector composable (Off / Daily / Weekly / Monthly / Yearly).
- **RecurrenceState**: The existing sealed interface representing the selected recurrence configuration.
- **Due_Date**: A date string in `YYYY-MM-DD` format representing when a main task is due.
- **Recurrence**: An RRULE-format string representing the recurrence rule for a main task.
- **Back_Stack**: The AndroidX Navigation 3 back stack managed via `rememberNavBackStack`.

## Requirements

### Requirement 1: Navigation Route

**User Story:** As a developer, I want a dedicated navigation route for the main task settings screen, so that the app can navigate to it using the existing navigation infrastructure.

#### Acceptance Criteria

1. THE MainTaskSettingsRoute SHALL be a `@Serializable` data class implementing `NavKey` with parameters `mainTaskId: Long`, `currentDueDate: String`, and `currentRecurrence: String`.
2. THE MainTaskSettingsRoute SHALL be registered in the `navKeySerializersModule` polymorphic serializer block.
3. WHEN the MainTaskSettingsRoute is added to the Back_Stack, THE NavDisplay SHALL render the Settings_Screen.

### Requirement 2: Settings Screen Package and Structure

**User Story:** As a developer, I want the settings screen code organized in its own package, so that it follows the existing project structure conventions.

#### Acceptance Criteria

1. THE Settings_Screen composable, Settings_ViewModel, and related types SHALL reside in the `net.onefivefour.echolist.ui.maintasksettings` package.
2. THE Settings_ViewModel SHALL be registered in the Koin `navigationModule` using the `viewModel` DSL with parameterized injection.

### Requirement 3: Settings Screen UI

**User Story:** As a user, I want a full-screen view for editing due date and recurrence settings, so that I have more space and a clearer interface for these controls.

#### Acceptance Criteria

1. THE Settings_Screen SHALL display a Material 3 DatePicker initialized with the current due date passed via the route.
2. THE Settings_Screen SHALL display the RecurrenceIntervalPicker below the DatePicker.
3. WHEN a recurrence interval other than Off is selected, THE Settings_Screen SHALL display the corresponding detail content composable (DailyDetailContent, WeeklyDetailContent, or MonthlyDetailContent).
4. THE Settings_Screen SHALL display a top bar with a back/close action that navigates back without applying changes.
5. THE Settings_Screen SHALL display a confirm action that applies the selected due date or recurrence and navigates back.

### Requirement 4: Settings ViewModel

**User Story:** As a developer, I want a dedicated ViewModel for the settings screen, so that it manages its own state independently from the task list editor.

#### Acceptance Criteria

1. THE Settings_ViewModel SHALL accept `mainTaskId: Long`, `initialDueDate: String`, and `initialRecurrence: String` as constructor parameters.
2. THE Settings_ViewModel SHALL expose a `StateFlow` of the current due date selection and recurrence state.
3. WHEN the user selects a date in the DatePicker, THE Settings_ViewModel SHALL update the due date state.
4. WHEN the user changes the recurrence interval or detail parameters, THE Settings_ViewModel SHALL update the recurrence state.
5. THE Settings_ViewModel SHALL expose a confirmation result containing `mainTaskId`, the selected due date, and the selected recurrence string.

### Requirement 5: Due Date and Recurrence Mutual Exclusion

**User Story:** As a user, I want due date and recurrence to remain mutually exclusive on the settings screen, so that the behavior is consistent with the existing domain model.

#### Acceptance Criteria

1. WHEN the user selects a date in the DatePicker while a recurrence is active, THE Settings_ViewModel SHALL clear the recurrence state to Off.
2. WHEN the user selects a recurrence interval other than Off while a due date is set, THE Settings_ViewModel SHALL clear the due date state.

### Requirement 6: Navigation from Edit Screen

**User Story:** As a user, I want to tap the calendar icon on a main task card and be taken to the settings screen, so that I can configure due date and recurrence in a dedicated view.

#### Acceptance Criteria

1. WHEN the user taps the calendar icon or the due date tag on a MainTaskCard, THE Edit_Screen SHALL navigate to the MainTaskSettingsRoute with the current main task's id, due date, and recurrence values.
2. THE Edit_Screen SHALL remove all bottom-sheet state management (`activeDateSheet`, `TaskDateSheetState`, and the `TaskDateBottomSheet` composable invocation).

### Requirement 7: Result Handling

**User Story:** As a user, I want my due date and recurrence changes to be applied to the correct main task when I confirm on the settings screen, so that my edits are saved.

#### Acceptance Criteria

1. WHEN the user confirms on the Settings_Screen, THE Settings_Screen SHALL pop itself from the Back_Stack.
2. WHEN the Settings_Screen result is received, THE Edit_Screen SHALL apply the returned due date and recurrence to the main task identified by `mainTaskId`.
3. IF the user navigates back without confirming, THEN THE Edit_Screen SHALL retain the main task's previous due date and recurrence values unchanged.

### Requirement 8: Bottom Sheet Removal

**User Story:** As a developer, I want the old bottom sheet code removed, so that there is a single code path for editing due date and recurrence settings.

#### Acceptance Criteria

1. THE `TaskDateBottomSheet` composable and `TaskDateSheetState` data class SHALL be removed from the `ui.edittasklist` package.
2. THE Edit_Screen SHALL contain no references to `TaskDateBottomSheet`, `TaskDateSheetState`, or `ModalBottomSheet` for due date and recurrence editing.
