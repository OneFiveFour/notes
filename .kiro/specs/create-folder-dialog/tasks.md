# Implementation Plan: Create Folder Dialog

## Overview

Implement a modal folder-creation dialog on the home screen, backed by a dedicated `CreateFolderViewModel`. Refactor `HomeScreen` parameters to use grouped callbacks, and add a repository-level `directoryChanged` signal so `HomeViewModel` auto-refreshes after mutations. All new code is Kotlin targeting `commonMain`, with property-based tests in `jvmTest` using Kotest.

## Tasks

- [x] 1. Add `directoryChanged` SharedFlow to FileRepository
  - [x] 1.1 Add `val directoryChanged: SharedFlow<String>` to `FileRepository` interface in `domain/repository/FileRepository.kt`
    - _Requirements: 4.5_
  - [x] 1.2 Implement `directoryChanged` in `FileRepositoryImpl`
    - Add `MutableSharedFlow<String>` backing field and expose as `SharedFlow`
    - Emit `params.parentDir` after successful `createFolder`, `updateFolder`, and `deleteFolder` calls
    - Do NOT emit on failure
    - _Requirements: 4.5, 2.2_
  - [xds] 1.3 Write property test for Property 6: Repository emits directoryChanged on successful mutation
    - **Property 6: Repository emits directoryChanged on successful mutation**
    - Generate arbitrary `CreateFolderParams` (arbitrary parentDir and name strings)
    - Mock `FileRemoteDataSource` to return success
    - Verify `directoryChanged` emits exactly the `parentDir`
    - Verify no emission on failure
    - **Validates: Requirements 4.5**

- [x] 2. Create `CreateFolderUiState` and `CreateFolderViewModel`
  - [x] 2.1 Create `CreateFolderUiState` data class in `ui/home/CreateFolderUiState.kt`
    - Fields: `isVisible: Boolean = false`, `folderName: String = ""`, `isLoading: Boolean = false`, `error: String? = null`
    - Computed property: `val isConfirmEnabled: Boolean get() = folderName.trim().isNotBlank() && !isLoading`
    - _Requirements: 1.4, 2.4, 3.2, 4.1_
  - [x] 2.2 Write property test for Property 2: Confirm button enabled iff valid and not loading
    - **Property 2: Confirm button enabled if and only if input is valid and not loading**
    - Generate arbitrary strings (including empty, whitespace-only, unicode) × arbitrary booleans for `isLoading`
    - Assert `isConfirmEnabled == (folderName.trim().isNotBlank() && !isLoading)`
    - **Validates: Requirements 1.4, 2.4, 3.2**
  - [x] 2.3 Create `CreateFolderViewModel` in `ui/home/CreateFolderViewModel.kt`
    - Constructor params: `currentPath: String`, `fileRepository: FileRepository` (injected via Koin)
    - Expose `uiState: StateFlow<CreateFolderUiState>`
    - Implement `showDialog()`: set `isVisible = true`, reset `folderName`, `error`, `isLoading`
    - Implement `dismissDialog()`: reset all state to defaults
    - Implement `onNameChange(value: String)`: update `folderName`, clear `error`
    - Implement `onConfirm()`: trim name, guard blank, set `isLoading = true`, call `fileRepository.createFolder(CreateFolderParams(currentPath, trimmedName))`, on success reset state, on failure set `error` to exception message and `isLoading = false`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 4.1, 4.2, 4.3_
  - [x] 2.4 Write property test for Property 1: Show/dismiss dialog round trip
    - **Property 1: Show/dismiss dialog round trip**
    - For any initial `CreateFolderUiState` where `isVisible = false`, calling `showDialog()` then `dismissDialog()` returns state to hidden with empty name, no error, no loading
    - **Validates: Requirements 1.1, 1.5**
  - [x] 2.5 Write property test for Property 3: Confirm sends trimmed name with correct path
    - **Property 3: Confirm sends trimmed name with correct path**
    - Generate non-blank strings with leading/trailing whitespace × arbitrary path strings
    - Mock `FileRepository` to capture `CreateFolderParams` argument
    - Verify `params.parentDir == currentPath` and `params.name == folderName.trim()`
    - **Validates: Requirements 2.1, 3.1**
  - [x] 2.6 Write property test for Property 4: Successful creation closes dialog
    - **Property 4: Successful creation closes dialog**
    - Generate non-blank folder names, mock `FileRepository.createFolder` to return `Result.success`
    - Verify state becomes `isVisible = false`, `folderName = ""`, `isLoading = false`, `error = null`
    - **Validates: Requirements 2.2**
  - [x] 2.7 Write property test for Property 5: Failed creation keeps dialog open with error
    - **Property 5: Failed creation keeps dialog open with error**
    - Generate non-blank folder names × arbitrary error messages
    - Mock `FileRepository.createFolder` to return `Result.failure`
    - Verify state has `isVisible = true`, `isLoading = false`, `error == exception.message`
    - **Validates: Requirements 2.3**

- [x] 3. Checkpoint
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Create `CreateFolderDialog` composable
  - [x] 4.1 Create `CreateFolderDialog` composable in `ui/home/CreateFolderDialog.kt`
    - Parameters: `uiState: CreateFolderUiState`, `onNameChange: (String) -> Unit`, `onConfirm: () -> Unit`, `onDismiss: () -> Unit`
    - Render only when `uiState.isVisible` is true
    - Use Material 3 `AlertDialog` with `EchoListTheme` tokens for all colors, typography, dimensions, and shapes
    - Text field: `ElOutlinedTextField` with auto-focus via `FocusRequester` and `LaunchedEffect`
    - Confirm button: `ElButton`, disabled when `!uiState.isConfirmEnabled`
    - Show `CircularProgressIndicator` in confirm button area when `isLoading`
    - Show error text below text field when `uiState.error != null`, using `EchoListTheme.materialColors.secondary` for error color
    - Dismiss on cancel button tap or outside tap via `onDismissRequest`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.3, 2.4_

- [ ] 5. Refactor HomeScreen parameters and wire dialog
  - [ ] 5.1 Create `CreateItemCallbacks` data class in `ui/home/CreateItemCallbacks.kt`
    - Fields: `onCreateFolder: () -> Unit = {}`, `onCreateNote: () -> Unit = {}`, `onCreateTaskList: () -> Unit = {}`
    - _Requirements: 5.1_
  - [ ] 5.2 Update `HomeScreen` composable signature
    - Replace `onCreateNote`, `onCreateTaskList`, `onCreateFolder` parameters with `createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks()`
    - Add `createFolderUiState: CreateFolderUiState` parameter
    - Add `onFolderNameChange: (String) -> Unit = {}`, `onConfirmCreateFolder: () -> Unit = {}`, `onDismissCreateFolder: () -> Unit = {}` parameters
    - Pass `createItemCallbacks` fields through to `BottomNavigation`
    - Render `CreateFolderDialog` inside `HomeScreen` using the new parameters
    - Update `BottomNavigation` to accept `CreateItemCallbacks` instead of individual lambdas
    - Update `CreateItemPills` to accept `CreateItemCallbacks` instead of individual lambdas
    - Update preview composables to use the new parameter surface
    - _Requirements: 5.1, 5.2_

- [ ] 6. Update HomeViewModel to observe `directoryChanged`
  - [ ] 6.1 Add `directoryChanged` collection to `HomeViewModel.init`
    - Launch a coroutine in `viewModelScope` that collects `fileRepository.directoryChanged`
    - When emitted path matches `this.path`, call `loadData()` to refresh
    - When emitted path does not match, ignore
    - _Requirements: 2.2, 4.4, 4.5_
  - [ ] 6.2 Write property test for Property 7: HomeViewModel refreshes on matching directoryChanged signal
    - **Property 7: HomeViewModel refreshes on matching directoryChanged signal**
    - Generate arbitrary path strings for the ViewModel and for the emitted signal
    - Create `HomeViewModel` with a mock `FileRepository` that has a controllable `directoryChanged` flow
    - Emit matching and non-matching paths
    - Verify `listFiles` is called again only for matching paths
    - **Validates: Requirements 2.2, 4.5**

- [ ] 7. Koin registration and App.kt wiring
  - [ ] 7.1 Register `CreateFolderViewModel` in `navigationModule` in `AppModules.kt`
    - Add `viewModel { params -> CreateFolderViewModel(currentPath = params.get(), fileRepository = get()) }`
    - _Requirements: 4.3, 5.3_
  - [ ] 7.2 Update `entry<HomeRoute>` in `App.kt`
    - Create `CreateFolderViewModel` via `koinViewModel` with key `"createFolder-${route.path}"` and `parametersOf(route.path)`
    - Collect `createFolderViewModel.uiState` with `collectAsStateWithLifecycle`
    - Pass `createFolderUiState`, `CreateItemCallbacks(onCreateFolder = createFolderViewModel::showDialog)`, `onFolderNameChange = createFolderViewModel::onNameChange`, `onConfirmCreateFolder = createFolderViewModel::onConfirm`, `onDismissCreateFolder = createFolderViewModel::dismissDialog` to `HomeScreen`
    - _Requirements: 5.2, 5.3_

- [ ] 8. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests use Kotest property-based testing (`kotest-property`) in `jvmTest`
- All composables follow the stateless pattern: receive state, emit callbacks
- All theme references use `EchoListTheme.*` tokens, no raw Material or dp literals
