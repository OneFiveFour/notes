# Requirements Document

## Introduction

This feature adds two editor screens to EchoList: one for creating notes and one for creating task lists. Each screen is navigated to from the home screen, accepts the parent folder path as context, presents a title text field and a save button, and persists the new item to the backend via the existing repository layer. Each screen has its own dedicated ViewModel. The navigation routes (`EditNoteRoute`, `EditTaskListRoute`) already exist but their screen entries are currently empty stubs.

## Glossary

- **Edit_Note_Screen**: The Compose screen for creating and editing a note.
- **Edit_TaskList_Screen**: The Compose screen for creating and editing a task list.
- **Edit_Note_ViewModel**: The ViewModel that manages UI state and backend interaction for the Edit_Note_Screen.
- **Edit_TaskList_ViewModel**: The ViewModel that manages UI state and backend interaction for the Edit_TaskList_Screen.
- **NotesRepository**: The repository interface providing CRUD operations for notes.
- **TaskListRepository**: The repository interface providing CRUD operations for task lists.
- **Home_Screen**: The existing folder browser screen from which users navigate to the editor screens.
- **Parent_Folder_Path**: The file-system path of the folder in which the new note or task list is created.
- **Save_Button**: The UI control that triggers persistence of the new item to the backend.
- **Title_Field**: The text input field where the user enters the name of the new note or task list.
- **TextFieldState**: The Compose Foundation state-based TextField API (`androidx.compose.foundation.text.input.TextFieldState`) that manages text input state internally, as opposed to the value-based `value`/`onValueChange` pattern.

## Requirements

### Requirement 1: Navigate to the Edit Note Screen

**User Story:** As a user, I want to navigate from the home screen to the note editor, so that I can create a new note in the current folder.

#### Acceptance Criteria

1. WHEN the user taps the create-note action on the Home_Screen, THE Home_Screen SHALL push an `EditNoteRoute` onto the navigation back stack with the current Parent_Folder_Path.
2. WHEN an `EditNoteRoute` is at the top of the back stack, THE App SHALL display the Edit_Note_Screen.
3. THE Edit_Note_Screen SHALL receive the Parent_Folder_Path from the route so that the Edit_Note_ViewModel knows where to create the note.

### Requirement 2: Navigate to the Edit TaskList Screen

**User Story:** As a user, I want to navigate from the home screen to the task list editor, so that I can create a new task list in the current folder.

#### Acceptance Criteria

1. WHEN the user taps the create-task-list action on the Home_Screen, THE Home_Screen SHALL push an `EditTaskListRoute` onto the navigation back stack with the current Parent_Folder_Path.
2. WHEN an `EditTaskListRoute` is at the top of the back stack, THE App SHALL display the Edit_TaskList_Screen.
3. THE Edit_TaskList_Screen SHALL receive the Parent_Folder_Path from the route so that the Edit_TaskList_ViewModel knows where to create the task list.

### Requirement 3: Edit Note Screen Layout

**User Story:** As a user, I want a simple note creation screen with a title field and a save button, so that I can quickly name and save a new note.

#### Acceptance Criteria

1. THE Edit_Note_Screen SHALL display a Title_Field for entering the note title.
2. THE Title_Field SHALL use the state-based TextField API backed by a `TextFieldState` instance, not the value-based `value`/`onValueChange` pattern.
3. THE Edit_Note_Screen SHALL display a Save_Button.
4. THE Edit_Note_Screen SHALL be a stateless composable that receives its UI state (including the `TextFieldState` for the title) and emits callbacks.
5. THE Edit_Note_Screen SHALL use `EchoListTheme` tokens for all colors, typography, spacing, and shapes.

### Requirement 4: Edit TaskList Screen Layout

**User Story:** As a user, I want a simple task list creation screen with a title field and a save button, so that I can quickly name and save a new task list.

#### Acceptance Criteria

1. THE Edit_TaskList_Screen SHALL display a Title_Field for entering the task list name.
2. THE Title_Field SHALL use the state-based TextField API backed by a `TextFieldState` instance, not the value-based `value`/`onValueChange` pattern.
3. THE Edit_TaskList_Screen SHALL display a Save_Button.
4. THE Edit_TaskList_Screen SHALL be a stateless composable that receives its UI state (including the `TextFieldState` for the name) and emits callbacks.
5. THE Edit_TaskList_Screen SHALL use `EchoListTheme` tokens for all colors, typography, spacing, and shapes.

### Requirement 5: Edit Note ViewModel

**User Story:** As a user, I want the note editor to manage my input and communicate with the backend, so that my note is persisted when I save.

#### Acceptance Criteria

1. THE Edit_Note_ViewModel SHALL expose a `StateFlow` of UI state containing a `TextFieldState` for the title, a loading flag, and an optional error message.
2. THE Edit_Note_ViewModel SHALL own the `TextFieldState` instance for the title so that text changes are managed by the Compose text input system without manual `onValueChange` handling.
3. WHEN the user taps the Save_Button and the `TextFieldState` text for the title is not blank, THE Edit_Note_ViewModel SHALL call `NotesRepository.createNote` with the trimmed title text, empty content, and the Parent_Folder_Path.
4. WHILE the create-note network request is in progress, THE Edit_Note_ViewModel SHALL set the loading flag to true in the UI state.
5. WHEN `NotesRepository.createNote` returns a successful result, THE Edit_Note_ViewModel SHALL signal the UI to navigate back to the Home_Screen.
6. IF `NotesRepository.createNote` returns a failure result, THEN THE Edit_Note_ViewModel SHALL set the error message in the UI state and set the loading flag to false.
7. WHEN the user taps the Save_Button and the `TextFieldState` text for the title is blank, THE Edit_Note_ViewModel SHALL not call `NotesRepository.createNote`.

### Requirement 6: Edit TaskList ViewModel

**User Story:** As a user, I want the task list editor to manage my input and communicate with the backend, so that my task list is persisted when I save.

#### Acceptance Criteria

1. THE Edit_TaskList_ViewModel SHALL expose a `StateFlow` of UI state containing a `TextFieldState` for the name, a loading flag, and an optional error message.
2. THE Edit_TaskList_ViewModel SHALL own the `TextFieldState` instance for the name so that text changes are managed by the Compose text input system without manual `onValueChange` handling.
3. WHEN the user taps the Save_Button and the `TextFieldState` text for the name is not blank, THE Edit_TaskList_ViewModel SHALL call `TaskListRepository.createTaskList` with the trimmed name text, the Parent_Folder_Path, and an empty task list.
4. WHILE the create-task-list network request is in progress, THE Edit_TaskList_ViewModel SHALL set the loading flag to true in the UI state.
5. WHEN `TaskListRepository.createTaskList` returns a successful result, THE Edit_TaskList_ViewModel SHALL signal the UI to navigate back to the Home_Screen.
6. IF `TaskListRepository.createTaskList` returns a failure result, THEN THE Edit_TaskList_ViewModel SHALL set the error message in the UI state and set the loading flag to false.
7. WHEN the user taps the Save_Button and the `TextFieldState` text for the name is blank, THE Edit_TaskList_ViewModel SHALL not call `TaskListRepository.createTaskList`.

### Requirement 7: Koin Dependency Injection Registration

**User Story:** As a developer, I want the new ViewModels to be registered in Koin, so that they can be injected into the Compose navigation entries.

#### Acceptance Criteria

1. THE navigationModule SHALL register Edit_Note_ViewModel with the Parent_Folder_Path as a parameter.
2. THE navigationModule SHALL register Edit_TaskList_ViewModel with the Parent_Folder_Path as a parameter.
3. WHEN the `EditNoteRoute` entry is composed, THE App SHALL create the Edit_Note_ViewModel via `koinViewModel` with the Parent_Folder_Path from the route.
4. WHEN the `EditTaskListRoute` entry is composed, THE App SHALL create the Edit_TaskList_ViewModel via `koinViewModel` with the Parent_Folder_Path from the route.

### Requirement 8: Save Button Enablement

**User Story:** As a user, I want the save button to be disabled when I cannot save, so that I do not trigger invalid save operations.

#### Acceptance Criteria

1. WHILE the `TextFieldState` text for the title in the Edit_Note_Screen is blank, THE Save_Button SHALL be disabled.
2. WHILE the loading flag is true in the Edit_Note_Screen, THE Save_Button SHALL be disabled.
3. WHILE the `TextFieldState` text for the name in the Edit_TaskList_Screen is blank, THE Save_Button SHALL be disabled.
4. WHILE the loading flag is true in the Edit_TaskList_Screen, THE Save_Button SHALL be disabled.

### Requirement 9: Route Parameter Extension

**User Story:** As a developer, I want the existing navigation routes to carry the parent folder path, so that the editor screens know where to create items.

#### Acceptance Criteria

1. THE `EditNoteRoute` SHALL include a `parentPath` parameter of type `String`.
2. THE `EditTaskListRoute` SHALL include a `parentPath` parameter of type `String`.
3. THE `EditNoteRoute` SHALL remain serializable for saved-state persistence after adding the `parentPath` parameter.
4. THE `EditTaskListRoute` SHALL remain serializable for saved-state persistence after adding the `parentPath` parameter.
