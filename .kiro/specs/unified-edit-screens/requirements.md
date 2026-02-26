# Requirements Document

## Introduction

EchoList currently has separate routes and screens for creating and viewing/editing notes and tasklists: `NoteDetailRoute`/`NoteDetailScreen`, `NoteCreateRoute`/`NoteCreateScreen`, and `TasklistDetailRoute`/`TasklistDetailScreen`. This feature consolidates each file type into a single unified route and screen pair — `EditNoteRoute`/`EditNoteScreen` and `EditTaskListRoute`/`EditTaskListScreen` — where an optional ID parameter determines whether the screen operates in create mode (no ID) or edit mode (ID provided). The old routes and screens are removed.

## Glossary

- **EditNoteRoute**: A `NavKey` data class with an optional `noteId: String?` parameter that navigates to the `EditNoteScreen`. Replaces `NoteDetailRoute` and `NoteCreateRoute`.
- **EditTaskListRoute**: A `NavKey` data class with an optional `taskListId: String?` parameter that navigates to the `EditTaskListScreen`. Replaces `TasklistDetailRoute`.
- **EditNoteScreen**: A Compose screen that handles both creating and viewing/editing notes, depending on whether a `noteId` is provided.
- **EditTaskListScreen**: A Compose screen that handles both creating and viewing/editing tasklists, depending on whether a `taskListId` is provided.
- **Router**: The navigation system defined in `Routes.kt`, including the `navKeySerializersModule` and `echoListSavedStateConfig`.
- **NavDisplay**: The `androidx.navigation3` component in `App.kt` that resolves routes to screen composables via `entryProvider`.
- **BackStack**: The mutable list of `NavKey` instances managed by `rememberNavBackStack` in `App.kt`.
- **Create_Mode**: The screen state when no ID is passed to the route (the route's ID parameter is `null`).
- **Edit_Mode**: The screen state when an ID is passed to the route (the route's ID parameter is non-null). Note: actual data loading for Edit_Mode is out of scope for this refactor.

## Requirements

### Requirement 1: Unified Edit Note Route

**User Story:** As a developer, I want a single `EditNoteRoute` that accepts an optional `noteId` parameter, so that one route handles both note creation and note editing navigation.

#### Acceptance Criteria

1. THE Router SHALL define an `EditNoteRoute` data class that implements `NavKey` and has a `noteId: String?` parameter defaulting to `null`.
2. THE Router SHALL include `EditNoteRoute` in the `navKeySerializersModule` polymorphic registration.
3. THE Router SHALL NOT define `NoteDetailRoute` or `NoteCreateRoute`.
4. WHEN `EditNoteRoute` is serialized to JSON and deserialized back, THE Router SHALL produce an object equal to the original for any value of `noteId` (round-trip property).

### Requirement 2: Unified Edit TaskList Route

**User Story:** As a developer, I want a single `EditTaskListRoute` that accepts an optional `taskListId` parameter, so that one route handles both tasklist creation and tasklist editing navigation.

#### Acceptance Criteria

1. THE Router SHALL define an `EditTaskListRoute` data class that implements `NavKey` and has a `taskListId: String?` parameter defaulting to `null`.
2. THE Router SHALL include `EditTaskListRoute` in the `navKeySerializersModule` polymorphic registration.
3. THE Router SHALL NOT define `TasklistDetailRoute`.
4. WHEN `EditTaskListRoute` is serialized to JSON and deserialized back, THE Router SHALL produce an object equal to the original for any value of `taskListId` (round-trip property).

### Requirement 3: Edit Note Screen

**User Story:** As a developer, I want a single `EditNoteScreen` composable that serves both create and edit modes, so that the note UI is consolidated into one screen.

#### Acceptance Criteria

1. THE EditNoteScreen SHALL accept a `noteId: String?` parameter, a `text: String` parameter, an `onTextChanged: (String) -> Unit` callback, an `onSaveClick: () -> Unit` callback, and an `onBackClick: () -> Unit` callback.
2. WHEN `noteId` is `null`, THE EditNoteScreen SHALL display a title of "New Note", an editable text field, and a save button (Create_Mode).
3. WHEN `noteId` is non-null, THE EditNoteScreen SHALL display a title of "Edit Note", an editable text field, and a save button (Edit_Mode).
4. THE EditNoteScreen SHALL use `EchoListTheme.materialColors`, `EchoListTheme.typography`, and `EchoListTheme.dimensions` for all styling.
5. THE EditNoteScreen SHALL display a back navigation icon that invokes the `onBackClick` callback when pressed.
6. THE EditNoteScreen SHALL reside in the `net.onefivefour.echolist.ui.editnote` package.

### Requirement 4: Edit TaskList Screen

**User Story:** As a developer, I want a single `EditTaskListScreen` composable that serves both create and edit modes, so that the tasklist UI is consolidated into one screen.

#### Acceptance Criteria

1. THE EditTaskListScreen SHALL accept a `taskListId: String?` parameter, a `text: String` parameter, an `onTextChanged: (String) -> Unit` callback, an `onSaveClick: () -> Unit` callback, and an `onBackClick: () -> Unit` callback.
2. WHEN `taskListId` is `null`, THE EditTaskListScreen SHALL display a title of "New Tasklist", an editable text field, and a save button (Create_Mode).
3. WHEN `taskListId` is non-null, THE EditTaskListScreen SHALL display a title of "Edit Tasklist", an editable text field, and a save button (Edit_Mode).
4. THE EditTaskListScreen SHALL use `EchoListTheme.materialColors`, `EchoListTheme.typography`, and `EchoListTheme.dimensions` for all styling.
5. THE EditTaskListScreen SHALL display a back navigation icon that invokes the `onBackClick` callback when pressed.
6. THE EditTaskListScreen SHALL reside in the `net.onefivefour.echolist.ui.edittasklist` package.

### Requirement 5: Navigation Wiring

**User Story:** As a developer, I want the `NavDisplay` entry providers in `App.kt` to use the new unified routes, so that navigation works correctly with the consolidated screens.

#### Acceptance Criteria

1. THE NavDisplay SHALL register an entry provider for `EditNoteRoute` that renders `EditNoteScreen` with the route's `noteId`.
2. THE NavDisplay SHALL register an entry provider for `EditTaskListRoute` that renders `EditTaskListScreen` with the route's `taskListId`.
3. THE NavDisplay SHALL NOT register entry providers for `NoteDetailRoute`, `NoteCreateRoute`, or `TasklistDetailRoute`.
4. WHEN the "add note" action is triggered from `HomeScreen`, THE BackStack SHALL receive an `EditNoteRoute` with `noteId` set to `null`.
5. WHEN the "add tasklist" action is triggered from `HomeScreen`, THE BackStack SHALL receive an `EditTaskListRoute` with `taskListId` set to `null`.
6. WHEN a file item is clicked on `HomeScreen`, THE BackStack SHALL receive an `EditNoteRoute` with `noteId` set to the file's ID.
7. WHEN the back action is triggered from `EditNoteScreen` or `EditTaskListScreen`, THE BackStack SHALL remove the top entry.

### Requirement 6: Removal of Old Screens and Routes

**User Story:** As a developer, I want the old separate route and screen files removed, so that the codebase has no dead code from the previous structure.

#### Acceptance Criteria

1. THE codebase SHALL NOT contain `NoteCreateScreen.kt` or the `net.onefivefour.echolist.ui.notecreate` package.
2. THE codebase SHALL NOT contain `NoteDetailScreen.kt`, `NoteDetailUiState.kt`, or the `net.onefivefour.echolist.ui.notedetail` package.
3. THE codebase SHALL NOT contain `TasklistDetailScreen.kt` or the `net.onefivefour.echolist.ui.tasklistdetail` package.
4. THE codebase SHALL NOT contain `NoteDetailViewModel.kt` in any package.

### Requirement 7: Test Updates

**User Story:** As a developer, I want the existing property tests updated to use the new routes, so that test coverage remains intact after the refactor.

#### Acceptance Criteria

1. THE `RouteSerializationPropertyTest` SHALL include round-trip serialization tests for `EditNoteRoute` with arbitrary `noteId` values including `null`.
2. THE `RouteSerializationPropertyTest` SHALL include round-trip serialization tests for `EditTaskListRoute` with arbitrary `taskListId` values including `null`.
3. THE `RouteSerializationPropertyTest` SHALL NOT reference `NoteDetailRoute`, `NoteCreateRoute`, or `TasklistDetailRoute`.
4. THE `NavigationPropertyTest` SHALL verify that adding a note pushes `EditNoteRoute(noteId = null)` onto the BackStack.
5. THE `NavigationPropertyTest` SHALL verify that adding a tasklist pushes `EditTaskListRoute(taskListId = null)` onto the BackStack.
6. THE `NavigationPropertyTest` SHALL NOT reference `NoteCreateRoute` or `TasklistDetailRoute`.
