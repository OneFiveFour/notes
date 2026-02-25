# Implementation Plan: Add New Item Inline

## Overview

Implement inline folder creation on the HomeScreen. The work proceeds bottom-up: state model first, then ViewModel logic, then UI composables, then DI wiring. Each step builds on the previous one so there is no orphaned code.

## Tasks

- [x] 1. Add InlineCreationState and update HomeScreenUiState
  - [x] 1.1 Define the `InlineCreationState` sealed interface in `HomeScreenUiState.kt`
    - Add `Hidden`, `Editing(name)`, `Saving(name)`, and `Error(name, message)` variants
    - Add `inlineCreationState: InlineCreationState = InlineCreationState.Hidden` field to `HomeScreenUiState`
    - _Requirements: 2.1, 4.2, 4.5, 5.2_

- [x] 2. Extend HomeViewModel with inline creation logic
  - [x] 2.1 Add `FolderRepository` as a constructor dependency to `HomeViewModel`
    - Accept `FolderRepository` alongside the existing `NotesRepository` parameter
    - Store the current `path` for use in `CreateFolderParams`
    - _Requirements: 4.1, 5.2_

  - [x] 2.2 Implement `onAddFolderClicked()`, `onInlineNameChanged()`, `onInlineConfirm()`, and `onInlineCancel()`
    - `onAddFolderClicked()` transitions `inlineCreationState` from `Hidden` to `Editing("")`
    - `onInlineNameChanged(name)` updates the name in `Editing` state
    - `onInlineConfirm()` validates the name is non-blank, transitions to `Saving`, calls `FolderRepository.createFolder` with correct `CreateFolderParams`, reloads via `NotesRepository.listNotes` on success, transitions to `Error` on failure
    - `onInlineCancel()` transitions state back to `Hidden`
    - _Requirements: 2.1, 2.5, 3.1, 3.2, 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 2.3 Write property test: Hidden to Editing transition (Property 3)
    - **Property 3: Tapping add transitions state from Hidden to Editing**
    - **Validates: Requirements 2.1**
    - Create `HomeViewModelPropertyTest.kt` in `commonTest/.../ui/home/`
    - Create `FakeFolderRepository` test double in `commonTest`
    - Use `checkAll` with arbitrary `HomeScreenUiState` where `inlineCreationState` is `Hidden`

  - [x] 2.4 Write property test: Cancel restores Hidden (Property 4)
    - **Property 4: Cancel restores Hidden without modifying folders**
    - **Validates: Requirements 2.5**
    - Use `checkAll` with arbitrary `InlineCreationState` variants (`Editing`, `Error`)

  - [x] 2.5 Write property test: Whitespace-only names rejected (Property 5)
    - **Property 5: Whitespace-only names are rejected**
    - **Validates: Requirements 3.1**
    - Use `checkAll` with `Arb.string` filtered to whitespace-only strings

  - [x] 2.6 Write property test: Non-blank names transition to Saving (Property 6)
    - **Property 6: Non-blank names transition to Saving**
    - **Validates: Requirements 3.2, 4.2**
    - Use `checkAll` with `Arb.string` filtered to non-blank strings

  - [x] 2.7 Write property test: CreateFolderParams correctness (Property 7)
    - **Property 7: CreateFolderParams are constructed correctly**
    - **Validates: Requirements 4.1**
    - Verify `parentPath` equals current path and `name` equals confirmed folder name

  - [x] 2.8 Write property test: Successful creation resets state (Property 8)
    - **Property 8: Successful creation resets state and includes new folder**
    - **Validates: Requirements 4.3, 4.4**
    - Use fake repositories returning success, verify `Hidden` state and updated folder list

  - [x] 2.9 Write property test: Failed creation transitions to Error (Property 9)
    - **Property 9: Failed creation transitions to Error with name preserved**
    - **Validates: Requirements 4.5**
    - Use fake repository returning failure, verify `Error` state with preserved name

- [x] 3. Checkpoint - Verify ViewModel logic
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Create AddItemButton composable
  - [x] 4.1 Create `AddItemButton.kt` in `ui/home/`
    - Outlined card matching `FolderCard` dimensions: medium shape, primary-colored border, surface background
    - Centered `Icons.Default.Add` icon in primary color
    - Accept `onClick` and `modifier` parameters
    - _Requirements: 1.1, 1.2, 1.3_

- [x] 5. Create InlineItemEditor composable
  - [x] 5.1 Create `InlineItemEditor.kt` in `ui/home/`
    - Same outer shape as `FolderCard`: medium shape, primary border, surface background
    - Left icon container with primary background and small shape
    - `BasicTextField` styled with `MaterialTheme.typography.titleSmall`, auto-focused via `FocusRequester`
    - `BringIntoViewRequester` to scroll into view when keyboard appears on initial focus
    - Confirm icon (`Icons.Default.Check`) and cancel icon (`Icons.Default.Close`)
    - When `isLoading` is true, replace confirm icon with small `CircularProgressIndicator`
    - When `errorMessage` is non-null, show error text below input in `MaterialTheme.colorScheme.error`
    - Accept item-type-agnostic parameters: `icon`, `placeholder`, `onConfirm`, `onCancel`, `onValueChange`, `value`, `isLoading`, `errorMessage`
    - _Requirements: 2.2, 2.3, 2.4, 2.6, 4.2, 4.5, 5.1, 5.3_

- [x] 6. Update HomeScreen to integrate AddItemButton and InlineItemEditor
  - [x] 6.1 Modify `HomeScreen.kt` folder grid logic
    - When `inlineCreationState` is `Hidden`: append `AddItemButton` as the last grid item after all `FolderCard` items
    - When `inlineCreationState` is `Editing`, `Saving`, or `Error`: replace the `AddItemButton` cell with `InlineItemEditor`
    - Maintain 2-column layout: pad odd-count rows with a `Spacer`
    - Add `imePadding()` on the scrollable container for keyboard handling
    - Add new callback parameters: `onAddFolderClick`, `onInlineNameChanged`, `onInlineConfirm`, `onInlineCancel`
    - _Requirements: 1.1, 1.4, 2.1, 2.5, 2.6_

  - [x] 6.2 Write property test: AddButton is always last grid item (Property 1)
    - **Property 1: AddButton is always the last grid item**
    - **Validates: Requirements 1.1**
    - Use `checkAll` with `Arb.list(arbFolderUiModel, 0..20)` to verify grid item ordering

  - [x] 6.3 Write property test: Grid rows padded to 2 columns (Property 2)
    - **Property 2: Grid rows are always padded to 2 columns**
    - **Validates: Requirements 1.4**
    - Use `checkAll` with varying folder counts to verify row padding logic

- [x] 7. Update Koin DI wiring
  - [x] 7.1 Update `navigationModule` in `AppModules.kt`
    - Update `HomeViewModel` factory to inject `FolderRepository` alongside `NotesRepository`
    - _Requirements: 4.1_

- [x] 8. Wire ViewModel to HomeScreen in navigation
  - [x] 8.1 Connect ViewModel callbacks to HomeScreen at the call site
    - Pass `uiState.inlineCreationState` to HomeScreen
    - Wire `onAddFolderClick` → `viewModel.onAddFolderClicked()`
    - Wire `onInlineNameChanged` → `viewModel.onInlineNameChanged()`
    - Wire `onInlineConfirm` → `viewModel.onInlineConfirm()`
    - Wire `onInlineCancel` → `viewModel.onInlineCancel()`
    - _Requirements: 2.1, 2.5, 3.2, 4.1_

- [x] 9. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- The `FakeFolderRepository` test double is created alongside the first property test
- Existing `FakeNotesRepository` is reused for ViewModel tests
