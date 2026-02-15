# Implementation Plan: Compose Navigation 3 Integration

## Overview

Incrementally integrate Compose Navigation 3 into EchoList by first adding build dependencies, then defining routes, refactoring `App.kt` to use `NavDisplay`, creating the Note Detail screen, introducing ViewModels, and wiring everything through Koin. Each step builds on the previous one and ends with a working, compilable state.

## Tasks

- [x] 1. Add Navigation 3 and kotlinx-serialization dependencies
  - [x] 1.1 Add the `kotlinx-serialization` plugin to `composeApp/build.gradle.kts` and the root `build.gradle.kts` (or settings plugin block)
    - Add `kotlin("plugin.serialization")` plugin entry and version catalog reference
    - Add `kotlinx-serialization-json` library to version catalog and `commonMain` dependencies
    - _Requirements: 1.1_
  - [x] 1.2 Add Navigation 3 libraries to the version catalog and `commonMain` dependencies
    - Add `navigation3-ui` (version `1.0.0-alpha05`) to `libs.versions.toml`
    - Add `lifecycle-viewmodel-navigation3` to `libs.versions.toml`
    - Add both as `commonMain` dependencies in `build.gradle.kts`
    - _Requirements: 1.2, 1.3, 1.4_

- [x] 2. Define route keys and serialization configuration
  - [x] 2.1 Create `ui/navigation/Routes.kt` with `HomeRoute`, `NoteDetailRoute`, `navKeySerializersModule`, and `echoListSavedStateConfig`
    - `HomeRoute(path: String = "/")` implementing `NavKey` with `@Serializable`
    - `NoteDetailRoute(noteId: String)` implementing `NavKey` with `@Serializable`
    - Polymorphic `SerializersModule` registering both subtypes
    - `SavedStateConfiguration` using the serializers module
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [x] 2.2 Write property test for route serialization round-trip
    - **Property 1: Route serialization round-trip**
    - Use Kotest `checkAll` with `Arb.string()` to generate random `HomeRoute` and `NoteDetailRoute` instances
    - Verify `Json.decodeFromString(Json.encodeToString(route)) == route` using polymorphic serialization
    - Minimum 100 iterations
    - **Validates: Requirements 2.3, 2.4**

- [x] 3. Checkpoint — Verify build compiles on all targets
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Create NoteDetailScreen and its UI state
  - [ ] 4.1 Create `ui/notedetail/NoteDetailUiState.kt` sealed interface
    - `Loading`, `Success(title, content, lastUpdated)`, `Error(message)` variants
    - _Requirements: 6.1, 6.3_
  - [ ] 4.2 Create `ui/notedetail/NoteDetailScreen.kt` stateless composable
    - Receives `NoteDetailUiState` and `onBackClick` callback
    - Renders note title, content, and timestamp for `Success` state
    - Renders error message for `Error` state
    - Shows loading indicator for `Loading` state
    - Back arrow in `TopAppBar` triggers `onBackClick`
    - Follow EchoList design system: `MaterialTheme.colorScheme`, `EchoListTheme.typography`, `LocalDimensions`, `MaterialTheme.shapes`
    - _Requirements: 6.1, 6.2, 6.3, 8.2_

- [ ] 5. Create ViewModels
  - [ ] 5.1 Create `ui/home/HomeViewModel.kt`
    - Takes `path: String` and `NotesRepository` as constructor parameters
    - Exposes `uiState: StateFlow<HomeScreenUiState>`
    - Loads folder and file data for the given path from the repository in `viewModelScope`
    - Maps domain models to `FolderUiModel`, `FileUiModel`, `BreadcrumbItem`
    - Handles errors by emitting a state with empty lists
    - _Requirements: 7.1_
  - [ ] 5.2 Create `ui/notedetail/NoteDetailViewModel.kt`
    - Takes `noteId: String` and `NotesRepository` as constructor parameters
    - Exposes `uiState: StateFlow<NoteDetailUiState>`
    - Loads note by `noteId` from the repository in `viewModelScope`
    - Emits `Success` with mapped data or `Error` if note not found or exception occurs
    - _Requirements: 7.2, 6.3_
  - [ ]* 5.3 Write property test for missing note error state
    - **Property 5: Missing note produces error state**
    - Use Kotest `checkAll` with `Arb.string()` to generate random noteIds
    - Use a mock repository that returns null/throws for any noteId
    - Verify the ViewModel emits `NoteDetailUiState.Error` with a non-empty message
    - Minimum 100 iterations
    - **Validates: Requirements 6.3**
  - [ ]* 5.4 Write unit tests for HomeViewModel and NoteDetailViewModel
    - Test HomeViewModel emits correct UI state for a known path with mock data
    - Test NoteDetailViewModel emits Success for a valid noteId
    - Test NoteDetailViewModel emits Error for an invalid noteId
    - _Requirements: 7.1, 7.2, 6.3_

- [ ] 6. Register ViewModels in Koin
  - [ ] 6.1 Update `di/AppModules.kt` to add a `navigationModule` with ViewModel factories
    - Register `HomeViewModel` with `path` parameter
    - Register `NoteDetailViewModel` with `noteId` parameter
    - Add `navigationModule` to `appModules` list
    - _Requirements: 7.1, 7.2, 7.3_

- [ ] 7. Refactor App.kt to use NavDisplay
  - [ ] 7.1 Refactor `App.kt` to host `NavDisplay` with back stack and destination mappings
    - Create back stack with `rememberNavBackStack(echoListSavedStateConfig, HomeRoute())`
    - Set up `NavDisplay` with `onBack` handler
    - Wire `HomeRoute` entry to `HomeViewModel` + `HomeScreen` with navigation callbacks
    - Wire `NoteDetailRoute` entry to `NoteDetailViewModel` + `NoteDetailScreen` with back callback
    - Implement folder click → push `HomeRoute(folderId)`
    - Implement file click → push `NoteDetailRoute(fileId)`
    - Implement breadcrumb click → pop to matching `HomeRoute` or push new one
    - Implement back navigation → `removeLastOrNull()` (no-op on single entry)
    - Remove hardcoded sample data
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 8.1, 8.3_

- [ ] 8. Checkpoint — Verify full navigation flow
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Back stack navigation property tests
  - [ ]* 9.1 Write property test for push navigation
    - **Property 2: Push navigation grows the back stack**
    - Use Kotest `checkAll` with `Arb` for non-empty list of `NavKey` and a single `NavKey`
    - Verify stack size increases by one, top entry matches pushed key, previous entries unchanged
    - Minimum 100 iterations
    - **Validates: Requirements 4.1, 4.2**
  - [ ]* 9.2 Write property test for breadcrumb navigation
    - **Property 3: Breadcrumb navigation truncates the back stack**
    - Generate back stacks with known `HomeRoute` entries, pick a random path to navigate to
    - Verify stack is truncated to the matching entry, entries before it are unchanged
    - Minimum 100 iterations
    - **Validates: Requirements 4.3**
  - [ ]* 9.3 Write property test for back navigation
    - **Property 4: Back navigation removes the top entry**
    - Generate back stacks of varying sizes (> 1 for normal case, == 1 for edge case)
    - Verify size decreases by one for multi-entry stacks, remains unchanged for single-entry stacks
    - Minimum 100 iterations
    - **Validates: Requirements 5.1, 5.2, 5.3**

- [ ] 10. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Navigation 3 is alpha (1.0.0-alpha05) — API may change in future releases
