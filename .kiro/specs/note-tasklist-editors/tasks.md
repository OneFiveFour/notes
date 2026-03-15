# Implementation Plan: Note & Task List Editors

## Overview

Implement two editor screens (EditNote, EditTaskList) with ViewModels, navigation wiring, and Koin registration. The work proceeds bottom-up: route changes first, then ViewModels and UI state, then screen composables, then DI and navigation wiring in App.kt, and finally home screen integration.

## Tasks

- [x] 1. Verify route data objects are parameterless
  - [x] 1.1 Confirm `EditNoteRoute` and `EditTaskListRoute` are `@Serializable data object` in `Routes.kt`
    - Both routes are now parameterless data objects (no `parentPath`, `noteId`, or `taskListId` constructor parameters)
    - Ensure both routes remain `@Serializable` and implement `NavKey`
    - The `parentPath` will be passed to the ViewModels directly from the navigation entry (which has access to the current `HomeRoute.path`) rather than through the route itself
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [x] 1.2 Write property test for route serialization round-trip (Property 5)
    - **Property 5: Route serialization round-trip**
    - **Validates: Requirements 9.3, 9.4**
    - Serialize each data object route to JSON via `kotlinx.serialization`, deserialize, and assert equality
    - Place test in `jvmTest` using Kotest `FunSpec` with JUnit 5 runner

- [x] 2. Implement EditNote ViewModel and UI state
  - [x] 2.1 Create `EditNoteUiState` data class and `EditNoteViewModel` in `ui/editnote/` package
    - Create `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/editnote/EditNoteUiState.kt`
    - `EditNoteUiState` holds `titleState: TextFieldState`, `isLoading: Boolean`, `error: String?`, and computed `isSaveEnabled`
    - Create `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/editnote/EditNoteViewModel.kt`
    - ViewModel owns `TextFieldState`, exposes `StateFlow<EditNoteUiState>` and `SharedFlow<Unit>` for navigate-back
    - `onSaveClick()` guards on blank trimmed title, sets loading, calls `NotesRepository.createNote` with trimmed title, empty content, and parentPath
    - On success: emit navigate-back event. On failure: set error message and clear loading flag
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [x] 2.2 Write property test for save guard (Property 1 — EditNoteViewModel)
    - **Property 1: Save guard — repository called if and only if trimmed text is non-blank**
    - **Validates: Requirements 5.3, 5.7**
    - Use `Arb.string()` covering empty, whitespace-only, and non-blank strings
    - Use a fake `NotesRepository` that records call count and parameters
    - Assert: repository called exactly once with trimmed text when non-blank, zero times when blank

  - [x] 2.3 Write property test for successful save (Property 2 — EditNoteViewModel)
    - **Property 2: Successful save emits navigate-back event**
    - **Validates: Requirements 5.5**
    - Use `Arb.string().filter { it.isNotBlank() }` for title
    - Fake repository returns `Result.success`
    - Assert: `navigateBack` SharedFlow emits exactly one event

  - [x] 2.4 Write property test for failed save (Property 3 — EditNoteViewModel)
    - **Property 3: Failed save sets error and clears loading**
    - **Validates: Requirements 5.6**
    - Use `Arb.string().filter { it.isNotBlank() }` for title, `Arb.string()` for error message
    - Fake repository returns `Result.failure` with the generated message
    - Assert: `uiState.isLoading == false` and `uiState.error == message`

- [x] 3. Implement EditTaskList ViewModel and UI state
  - [x] 3.1 Create `EditTaskListUiState` data class and `EditTaskListViewModel` in `ui/edittasklist/` package
    - Create `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListUiState.kt`
    - `EditTaskListUiState` holds `titleState: TextFieldState`, `isLoading: Boolean`, `error: String?`, and computed `isSaveEnabled`
    - Create `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListViewModel.kt`
    - Same pattern as EditNoteViewModel but calls `TaskListRepository.createTaskList` with trimmed title, parentPath, and `emptyList()` for tasks
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_

  - [x] 3.2 Write property test for save guard (Property 1 — EditTaskListViewModel)
    - **Property 1: Save guard — repository called if and only if trimmed text is non-blank**
    - **Validates: Requirements 6.3, 6.7**
    - Same strategy as 2.2 but with fake `TaskListRepository`

  - [x] 3.3 Write property test for successful save (Property 2 — EditTaskListViewModel)
    - **Property 2: Successful save emits navigate-back event**
    - **Validates: Requirements 6.5**

  - [x] 3.4 Write property test for failed save (Property 3 — EditTaskListViewModel)
    - **Property 3: Failed save sets error and clears loading**
    - **Validates: Requirements 6.6**

- [x] 4. Write property test for save-enabled computation (Property 4)
  - [x] 4.1 Write property test for `EditNoteUiState.isSaveEnabled`
    - **Property 4: Save-enabled computation**
    - **Validates: Requirements 8.1, 8.2**
    - Use `Arb.string()` for text content and `Arb.boolean()` for isLoading
    - Assert: `isSaveEnabled == (text.isNotBlank() && !isLoading)`

  - [x] 4.2 Write property test for `EditTaskListUiState.isSaveEnabled`
    - **Property 4: Save-enabled computation**
    - **Validates: Requirements 8.3, 8.4**
    - Same strategy as 4.1 but for `EditTaskListUiState`

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement editor screen composables
  - [x] 6.1 Create `EditNoteScreen` composable in `ui/editnote/` package
    - Create `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/editnote/EditNoteScreen.kt`
    - Stateless composable: receives `EditNoteUiState` and `onSaveClick: () -> Unit`
    - Render title `BasicTextField` bound to `uiState.titleState`
    - Render save button enabled by `uiState.isSaveEnabled`
    - Display error message from `uiState.error` when present
    - Use `EchoListTheme.materialColors`, `EchoListTheme.typography`, `EchoListTheme.dimensions`, `EchoListTheme.shapes` for all styling
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 8.1, 8.2_

  - [x] 6.2 Create `EditTaskListScreen` composable in `ui/edittasklist/` package
    - Create `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListScreen.kt`
    - Same pattern as `EditNoteScreen` but for task list title
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 8.3, 8.4_

- [x] 7. Register ViewModels in Koin and wire navigation entries
  - [x] 7.1 Register `EditNoteViewModel` and `EditTaskListViewModel` in `navigationModule` in `AppModules.kt`
    - Add `viewModel { params -> EditNoteViewModel(parentPath = params.get(), notesRepository = get()) }`
    - Add `viewModel { params -> EditTaskListViewModel(parentPath = params.get(), taskListRepository = get()) }`
    - _Requirements: 7.1, 7.2_

  - [x] 7.2 Wire `EditNoteRoute` and `EditTaskListRoute` entries in `App.kt`
    - In the `entry<EditNoteRoute>` block: create ViewModel via `koinViewModel` with `parametersOf(currentPath)` (the parent path from the preceding `HomeRoute` in the back stack), collect `uiState`, collect `navigateBack` to pop back stack, render `EditNoteScreen`
    - In the `entry<EditTaskListRoute>` block: same pattern for `EditTaskListViewModel` and `EditTaskListScreen`
    - Since routes are now parameterless data objects, the `parentPath` must be derived from the current navigation context (e.g., the `HomeRoute.path` that was active when the user navigated)
    - _Requirements: 1.2, 1.3, 2.2, 2.3, 7.3, 7.4_

  - [x] 7.3 Wire home screen create-note and create-task-list navigation in `App.kt`
    - Update `CreateItemCallbacks` in the `HomeRoute` entry to push `EditNoteRoute` for `onCreateNote` and `EditTaskListRoute` for `onCreateTaskList` (no constructor parameters needed — routes are data objects)
    - _Requirements: 1.1, 2.1_

- [x] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- All tasks including property tests are mandatory
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- The implementation language is Kotlin (Compose Multiplatform) as established in the design
- Fake repository implementations should be created in `jvmTest` for property and unit tests
