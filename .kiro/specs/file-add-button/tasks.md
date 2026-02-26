# Implementation Plan: File Add Button

## Overview

Add a composite "add file" button to the home screen's files section with two sub-buttons (note and tasklist), plus two new detail screens (`NoteCreateScreen` and `TasklistDetailScreen`) with editable text fields and non-functional save buttons. Introduces `NoteCreateRoute` and `TasklistDetailRoute` navigation routes.

## Tasks

- [x] 1. Add new navigation routes
  - [x] 1.1 Add `NoteCreateRoute` and `TasklistDetailRoute` data objects to `Routes.kt`
    - Add `@Serializable data object NoteCreateRoute : NavKey` and `@Serializable data object TasklistDetailRoute : NavKey`
    - Register both in `navKeySerializersModule` polymorphic block
    - _Requirements: 3.1, 4.1_

  - [x] 1.2 Write property test for route serialization round-trip
    - **Property 5: Route serialization round-trip**
    - Extend `RouteSerializationPropertyTest.kt` with tests for `NoteCreateRoute` and `TasklistDetailRoute`
    - Verify serializing to JSON and deserializing back produces an equal object
    - **Validates: Requirements 3.1, 4.1**

- [x] 2. Create the AddFileButton composable
  - [x] 2.1 Implement `AddFileButton` composable in `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/home/AddFileButton.kt`
    - Stateless composable with `onAddNoteClick` and `onAddTasklistClick` callbacks
    - Secondary-colored border, medium shape, surface background (matching `FileItem` styling)
    - Two sub-buttons arranged horizontally in a `Row` with equal weight
    - Each sub-button has an icon and label text ("Note" and "Tasklist")
    - Sub-buttons separated by a vertical divider
    - Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `LocalEchoListDimensions` for all styling
    - _Requirements: 1.2, 1.3, 2.1, 2.2, 2.3, 2.4, 5.1, 5.2, 5.3, 5.6_

- [x] 3. Integrate AddFileButton into HomeScreen files section
  - [x] 3.1 Add `onAddNoteClick` and `onAddTasklistClick` callback parameters to `HomeScreen`
    - _Requirements: 2.1, 5.6_

  - [x] 3.2 Modify the FILES section rendering to append `AddFileButton` as the last item after all `FileItem` entries
    - Build a file grid cell pattern similar to the existing `FolderGridCell` approach, or simply append `AddFileButton` after the `forEachIndexed` loop
    - Ensure `AddFileButton` appears even when the files list is empty
    - _Requirements: 1.1_

  - [x] 3.3 Write property test for AddFileButton always being the last item
    - **Property 1: AddFileButton is always the last item in the files section**
    - Generate random lists of `FileUiModel`, build the files section items, assert count = `files.size + 1` and last item is `AddFileButton`
    - **Validates: Requirements 1.1**

- [x] 4. Checkpoint
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Create NoteCreateScreen composable
  - [x] 5.1 Implement `NoteCreateScreen` in `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/notecreate/NoteCreateScreen.kt`
    - Stateless composable with `text`, `onTextChanged`, `onSaveClick`, `onBackClick` parameters
    - `TopAppBar` with back button and title "New Note"
    - Editable `OutlinedTextField` for content
    - Save button (wired to `onSaveClick` callback)
    - Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `LocalEchoListDimensions` for all styling
    - _Requirements: 3.2, 3.3, 3.5, 5.4, 5.7_

- [x] 6. Create TasklistDetailScreen composable
  - [x] 6.1 Implement `TasklistDetailScreen` in `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/tasklistdetail/TasklistDetailScreen.kt`
    - Stateless composable with `text`, `onTextChanged`, `onSaveClick`, `onBackClick` parameters
    - `TopAppBar` with back button and title "New Tasklist"
    - Editable `OutlinedTextField` for content
    - Save button (wired to `onSaveClick` callback)
    - Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `LocalEchoListDimensions` for all styling
    - _Requirements: 4.2, 4.3, 4.5, 5.5, 5.8_

  - [x] 6.2 Write property test for save button no-op
    - **Property 3: Save button is a no-op (idempotence)**
    - Generate random strings as text content, invoke save callback, assert text unchanged
    - **Validates: Requirements 3.4, 4.4**

- [x] 7. Wire navigation in App.kt
  - [x] 7.1 Add `entry<NoteCreateRoute>` to `App.kt` entryProvider
    - Hold text state with `remember { mutableStateOf("") }`
    - Wire `NoteCreateScreen` with `onSaveClick = { /* no-op */ }` and `onBackClick = { backStack.removeLastOrNull() }`
    - _Requirements: 3.1, 3.4, 3.6_

  - [x] 7.2 Add `entry<TasklistDetailRoute>` to `App.kt` entryProvider
    - Hold text state with `remember { mutableStateOf("") }`
    - Wire `TasklistDetailScreen` with `onSaveClick = { /* no-op */ }` and `onBackClick = { backStack.removeLastOrNull() }`
    - _Requirements: 4.1, 4.4, 4.6_

  - [x] 7.3 Wire `HomeScreen` callbacks in the `entry<HomeRoute>` block
    - Pass `onAddNoteClick = { backStack.add(NoteCreateRoute) }` and `onAddTasklistClick = { backStack.add(TasklistDetailRoute) }`
    - _Requirements: 3.1, 4.1_

  - [x] 7.4 Write property test for sub-button navigation
    - **Property 2: Sub-button navigation pushes the correct route**
    - Use a fake back stack (`MutableList<NavKey>`), invoke add-note/add-tasklist callbacks, assert correct route on top and stack size increases by 1
    - **Validates: Requirements 3.1, 4.1**

  - [x] 7.5 Write property test for back navigation
    - **Property 4: Back navigation pops the detail route**
    - Generate random back stacks with `NoteCreateRoute` or `TasklistDetailRoute` on top, invoke back action, assert top removed and stack size decreases by 1
    - **Validates: Requirements 3.6, 4.6**

- [x] 8. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- The design language is Kotlin with Compose Multiplatform, matching the existing codebase
