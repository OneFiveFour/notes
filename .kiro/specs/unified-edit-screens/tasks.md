# Implementation Plan: Unified Edit Screens

## Overview

Refactor the EchoList navigation and screen layer to consolidate separate create/detail routes and screens into unified edit routes and screens. Each new route uses an optional ID parameter to determine create vs. edit mode. Old routes, screens, and view models are removed. All navigation wiring and property tests are updated.

## Tasks

- [x] 1. Define new unified routes and update serialization module
  - [x] 1.1 Replace old route definitions with `EditNoteRoute` and `EditTaskListRoute` in `Routes.kt`
    - Remove `NoteDetailRoute`, `NoteCreateRoute`, and `TasklistDetailRoute` data classes
    - Add `EditNoteRoute(noteId: String? = null) : NavKey` data class with `@Serializable`
    - Add `EditTaskListRoute(taskListId: String? = null) : NavKey` data class with `@Serializable`
    - Update `navKeySerializersModule` to register `EditNoteRoute` and `EditTaskListRoute` instead of the old routes
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_

  - [x] 1.2 Write property test for `EditNoteRoute` serialization round-trip
    - **Property 1: Route serialization round-trip (EditNoteRoute)**
    - Update `RouteSerializationPropertyTest.kt`: remove `NoteDetailRoute`, `NoteCreateRoute` tests; add `EditNoteRoute` round-trip tests with `Arb.string().orNull()` for `noteId`
    - Test both concrete type and polymorphic `NavKey` serialization
    - **Validates: Requirements 1.4, 7.1**

  - [x] 1.3 Write property test for `EditTaskListRoute` serialization round-trip
    - **Property 1: Route serialization round-trip (EditTaskListRoute)**
    - Update `RouteSerializationPropertyTest.kt`: remove `TasklistDetailRoute` tests; add `EditTaskListRoute` round-trip tests with `Arb.string().orNull()` for `taskListId`
    - Test both concrete type and polymorphic `NavKey` serialization
    - **Validates: Requirements 2.4, 7.2, 7.3**

- [x] 2. Checkpoint - Ensure route serialization tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 3. Create unified edit screens
  - [ ] 3.1 Create `EditNoteScreen` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/editnote/EditNoteScreen.kt`
    - Stateless composable accepting `noteId: String?`, `text: String`, `onTextChanged: (String) -> Unit`, `onSaveClick: () -> Unit`, `onBackClick: () -> Unit`, `modifier: Modifier`
    - Display title "New Note" when `noteId == null`, "Edit Note" when non-null
    - TopAppBar with back button (ArrowBack icon), `OutlinedTextField`, save button
    - Use `EchoListTheme.materialColors`, `EchoListTheme.typography`, `EchoListTheme.dimensions` for styling
    - Reference existing `NoteCreateScreen.kt` for layout structure
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ] 3.2 Create `EditTaskListScreen` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListScreen.kt`
    - Stateless composable accepting `taskListId: String?`, `text: String`, `onTextChanged: (String) -> Unit`, `onSaveClick: () -> Unit`, `onBackClick: () -> Unit`, `modifier: Modifier`
    - Display title "New Tasklist" when `taskListId == null`, "Edit Tasklist" when non-null
    - Same layout as `EditNoteScreen` but with tasklist-specific placeholder text
    - Use `EchoListTheme.materialColors`, `EchoListTheme.typography`, `EchoListTheme.dimensions` for styling
    - Reference existing `TasklistDetailScreen.kt` for layout structure
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [ ] 4. Update navigation wiring in `App.kt`
  - [ ] 4.1 Replace old entry providers with new unified route entries
    - Remove `entry<NoteDetailRoute>`, `entry<NoteCreateRoute>`, `entry<TasklistDetailRoute>` from `NavDisplay`
    - Add `entry<EditNoteRoute>` that renders `EditNoteScreen` with `route.noteId`
    - Add `entry<EditTaskListRoute>` that renders `EditTaskListScreen` with `route.taskListId`
    - Wire `onBackClick` to `backStack.removeLastOrNull()`
    - Wire `onSaveClick` as no-op
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ] 4.2 Update HomeScreen callback wiring in `App.kt`
    - Change `onAddNoteClick` to push `EditNoteRoute()` (noteId defaults to null)
    - Change `onAddTasklistClick` to push `EditTaskListRoute()` (taskListId defaults to null)
    - Change `onFileClick` to push `EditNoteRoute(noteId = fileId)`
    - _Requirements: 5.4, 5.5, 5.6, 5.7_

  - [ ] 4.3 Write property tests for create actions pushing null-ID routes
    - **Property 3: Create actions push null-ID routes**
    - Update `NavigationPropertyTest.kt`: replace `NoteCreateRoute` with `EditNoteRoute(noteId = null)`, `TasklistDetailRoute` with `EditTaskListRoute(taskListId = null)`
    - Update `Arb.navKey()` and `Arb.detailRoute()` generators to use new routes
    - **Validates: Requirements 5.4, 5.5, 7.4, 7.5, 7.6**

  - [ ] 4.4 Write property test for file click pushing `EditNoteRoute` with file ID
    - **Property 4: File click pushes EditNoteRoute with file ID**
    - Add test in `NavigationPropertyTest.kt`: generate random back stacks and non-empty file ID strings, invoke file-click action, assert `EditNoteRoute(noteId = fileId)` on top
    - **Validates: Requirements 5.6**

  - [ ] 4.5 Write property test for back navigation popping top entry
    - **Property 5: Back navigation pops top entry**
    - Update existing back-navigation test in `NavigationPropertyTest.kt` to use `EditNoteRoute`/`EditTaskListRoute` as detail routes
    - **Validates: Requirements 5.7**

- [ ] 5. Checkpoint - Ensure navigation tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Remove old screens, routes, view models, and tests
  - [ ] 6.1 Delete old screen and view model files
    - Delete `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/notecreate/NoteCreateScreen.kt`
    - Delete `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/notedetail/NoteDetailScreen.kt`
    - Delete `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/notedetail/NoteDetailUiState.kt`
    - Delete `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/notedetail/NoteDetailViewModel.kt`
    - Delete `composeApp/src/commonMain/kotlin/net/onefivefour/echolist/ui/tasklistdetail/TasklistDetailScreen.kt`
    - Remove any Koin module registrations for `NoteDetailViewModel`
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ] 6.2 Delete old test files referencing removed components
    - Delete `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/notedetail/NoteDetailViewModelPropertyTest.kt`
    - Delete `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/notedetail/NoteDetailViewModelTest.kt`
    - _Requirements: 6.4_

  - [ ] 6.3 Update `SaveButtonNoOpPropertyTest.kt` references
    - Update test descriptions from `NoteCreateScreen`/`TasklistDetailScreen` to `EditNoteScreen`/`EditTaskListScreen`
    - _Requirements: 7.3_

- [ ] 7. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- The design uses Kotlin with Compose Multiplatform — all code examples use Kotlin
- This is a pure refactoring — no new user-facing functionality is added
