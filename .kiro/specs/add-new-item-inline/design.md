# Design Document: Add New Item Inline

## Overview

This feature adds inline item creation to the HomeScreen folder grid. When the user taps an "add" button (the last cell in the 2-column grid), the button is replaced in-place by an editable card that looks like a regular `FolderCard`. The user types a name, confirms, and the system creates the folder via `FolderRepository.createFolder` over ConnectRPC. On success the folder list reloads; on failure an error is shown and the editor stays open.

The UI components and ViewModel state management are designed to be item-type-agnostic so the same pattern can be reused for note file creation later.

## Architecture

The feature touches three layers:

```
┌─────────────────────────────────────────────────┐
│  UI Layer (Compose)                             │
│  HomeScreen  ─►  AddItemButton                  │
│              ─►  InlineItemEditor               │
│              ─►  FolderCard (existing)           │
└──────────────────────┬──────────────────────────┘
                       │ callbacks / UiState
┌──────────────────────▼──────────────────────────┐
│  ViewModel Layer                                │
│  HomeViewModel                                  │
│    - InlineCreationState (sealed)               │
│    - onAddItemClicked()                         │
│    - onInlineNameChanged(name)                  │
│    - onInlineConfirm()                          │
│    - onInlineCancel()                           │
└──────────────────────┬──────────────────────────┘
                       │ suspend calls
┌──────────────────────▼──────────────────────────┐
│  Data Layer                                     │
│  FolderRepository.createFolder(params)          │
│  NotesRepository.listNotes(path)  (reload)      │
└─────────────────────────────────────────────────┘
```

### Key design decisions

1. **Replace-in-grid, not overlay**: The AddButton cell is swapped for the InlineItemEditor cell within the same `LazyVerticalGrid` / chunked-row layout. No dialogs or bottom sheets.
2. **Sealed interface for inline state**: `InlineCreationState` is a sealed interface (`Hidden`, `Editing`, `Saving`, `Error`) living inside `HomeScreenUiState`. This keeps the state machine explicit and testable without UI framework dependencies.
3. **Item-type-agnostic editor**: `InlineItemEditor` accepts a placeholder string, an icon `ImageVector`, and confirm/cancel lambdas. The ViewModel holds a generic `InlineCreationState` that can later be parameterised for note creation.

## Components and Interfaces

### New Composables

#### `AddItemButton`
Location: `ui/home/AddItemButton.kt`

```kotlin
@Composable
fun AddItemButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

- Outlined card matching `FolderCard` dimensions: medium shape, primary-colored border, surface background.
- Centered `Icons.Default.Add` icon in primary color.
- Occupies one grid cell (same `Modifier.weight(1f)` as `FolderCard`).

#### `InlineItemEditor`
Location: `ui/home/InlineItemEditor.kt`

```kotlin
@Composable
fun InlineItemEditor(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    icon: ImageVector,
    placeholder: String,
    modifier: Modifier = Modifier
)
```

- Same outer shape as `FolderCard`: medium shape, primary border, surface background.
- Left icon container: primary background, small shape (matches `FolderCard` icon box).
- `BasicTextField` styled with `MaterialTheme.typography.titleSmall`, auto-focused via `FocusRequester`.
- Uses `BringIntoViewRequester` to scroll itself into view when the soft keyboard appears. A `bringIntoViewRequester` modifier is attached to the editor root, and `bringIntoView()` is launched in a coroutine triggered by the initial focus request. This ensures the editor is visible above the keyboard on first appearance. No re-scrolling is performed if the user manually scrolls the editor out of view afterwards.
- Confirm icon (`Icons.Default.Check`) and cancel icon (`Icons.Default.Close`) on the right.
- When `isLoading` is true, the confirm icon is replaced by a small `CircularProgressIndicator`.
- When `errorMessage` is non-null, a `Text` in `MaterialTheme.colorScheme.error` is shown below the input.

### Modified Composables

#### `HomeScreen`
- The folder grid rendering logic gains awareness of `inlineCreationState`:
  - `Hidden` → append `AddItemButton` as the last grid item.
  - `Editing` / `Saving` / `Error` → replace the `AddItemButton` cell with `InlineItemEditor`.
- When transitioning to `Editing`, the grid should use `imePadding()` on the scrollable container so the keyboard does not overlap content, and the `InlineItemEditor` triggers `BringIntoViewRequester.bringIntoView()` on focus to guarantee it is visible above the keyboard.
- New callback parameters: `onAddFolderClick`, `onInlineNameChanged`, `onInlineConfirm`, `onInlineCancel`.

### Modified ViewModel

#### `HomeViewModel`
- Receives `FolderRepository` as a new constructor dependency (in addition to existing `NotesRepository`).
- Exposes the same `uiState: StateFlow<HomeScreenUiState>` but the data class gains an `inlineCreationState` field.
- New public functions:
  - `onAddFolderClicked()` → transitions state to `Editing`.
  - `onInlineNameChanged(name: String)` → updates the text in `Editing` state.
  - `onInlineConfirm()` → validates name, transitions to `Saving`, calls `FolderRepository.createFolder`, then reloads via `NotesRepository.listNotes`.
  - `onInlineCancel()` → transitions state back to `Hidden`.

### Koin DI Changes

`navigationModule` in `AppModules.kt`:
- `HomeViewModel` factory updated to inject `FolderRepository` alongside `NotesRepository`.

## Data Models

### New: `InlineCreationState` (sealed interface)
Location: `ui/home/HomeScreenUiState.kt`

```kotlin
sealed interface InlineCreationState {
    data object Hidden : InlineCreationState
    data class Editing(val name: String = "") : InlineCreationState
    data class Saving(val name: String) : InlineCreationState
    data class Error(val name: String, val message: String) : InlineCreationState
}
```

### Modified: `HomeScreenUiState`

```kotlin
data class HomeScreenUiState(
    val title: String,
    val breadcrumbs: List<BreadcrumbItem>,
    val folders: List<FolderUiModel>,
    val files: List<FileUiModel>,
    val inlineCreationState: InlineCreationState = InlineCreationState.Hidden
)
```

### Existing (unchanged)

- `CreateFolderParams(domain, parentPath, name)` — already exists in `data/models/`.
- `FolderRepository.createFolder(params): Result<List<Folder>>` — already exists.
- `NotesRepository.listNotes(path): Result<ListNotesResult>` — used for reload after creation.


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: AddButton is always the last grid item

*For any* list of folders (including empty), the grid items list produced by the HomeScreen layout logic should always place the AddButton as the final element, immediately after all FolderCard items.

**Validates: Requirements 1.1**

### Property 2: Grid rows are always padded to 2 columns

*For any* number of folders N, when the total item count (N + 1 for the AddButton or InlineEditor) is odd, the last row should contain exactly one item and one spacer, maintaining the 2-column layout invariant.

**Validates: Requirements 1.4**

### Property 3: Tapping add transitions state from Hidden to Editing

*For any* `HomeScreenUiState` where `inlineCreationState` is `Hidden`, calling `onAddFolderClicked()` should result in `inlineCreationState` becoming `Editing` with an empty name.

**Validates: Requirements 2.1**

### Property 4: Cancel restores Hidden without modifying folders

*For any* `InlineCreationState` that is `Editing`, `Saving`, or `Error`, calling `onInlineCancel()` should transition `inlineCreationState` to `Hidden` and the folder list should remain identical to what it was before the cancel.

**Validates: Requirements 2.5**

### Property 5: Whitespace-only names are rejected

*For any* string composed entirely of whitespace characters (including the empty string), calling `onInlineConfirm()` while in `Editing` state should leave the state as `Editing` with the name unchanged, and no repository call should be made.

**Validates: Requirements 3.1**

### Property 6: Non-blank names transition to Saving

*For any* non-blank string (containing at least one non-whitespace character), calling `onInlineConfirm()` while in `Editing` state should transition `inlineCreationState` to `Saving` with the trimmed name.

**Validates: Requirements 3.2, 4.2**

### Property 7: CreateFolderParams are constructed correctly

*For any* valid folder name and any current path, when the ViewModel initiates folder creation, the `CreateFolderParams` passed to `FolderRepository.createFolder` should have `parentPath` equal to the current path and `name` equal to the confirmed folder name.

**Validates: Requirements 4.1**

### Property 8: Successful creation resets state and includes new folder

*For any* valid folder name, when `FolderRepository.createFolder` returns success and `NotesRepository.listNotes` returns an updated list, the resulting `inlineCreationState` should be `Hidden` and the folder list should reflect the reloaded data.

**Validates: Requirements 4.3, 4.4**

### Property 9: Failed creation transitions to Error with name preserved

*For any* failure result from `FolderRepository.createFolder`, the `inlineCreationState` should transition to `Error` with a non-empty error message and the original folder name preserved, allowing retry or cancel.

**Validates: Requirements 4.5**

## Error Handling

| Scenario | Handling |
|---|---|
| Network failure on `createFolder` | Transition to `InlineCreationState.Error(name, message)`. The user sees the error below the text field and can retry or cancel. |
| Empty / whitespace-only name confirmed | Silently rejected — state stays `Editing`. No network call is made. |
| `listNotes` reload fails after successful creation | The folder was created server-side. Transition to `Hidden` and show the stale folder list. A pull-to-refresh or re-navigation will pick up the new folder. |
| User cancels during `Saving` | The cancel action is disabled while `isLoading` is true to prevent race conditions. The user must wait for the network call to complete or fail. |

## Testing Strategy

### Property-Based Testing

Library: **Kotest Property** (`io.kotest.property`)

Each correctness property above maps to a single Kotest property test. Tests use `checkAll` with Kotest `Arb` generators and run a minimum of 100 iterations.

Each test is tagged with a comment referencing the design property:
```
// Feature: add-new-item-inline, Property N: <property title>
```

Key generators needed:
- `Arb.string(0..100)` for folder names (including whitespace-only strings).
- `Arb.list(arbFolderUiModel, 0..20)` for folder lists of varying sizes.
- Custom `Arb` for `InlineCreationState` variants.
- Custom `Arb` for `HomeScreenUiState` with constrained valid states.

Test file: `commonTest/.../ui/home/HomeViewModelPropertyTest.kt`

### Unit Testing

Unit tests complement property tests for specific examples, integration points, and edge cases:

- **HomeViewModelTest** (existing, extended):
  - Verify initial state has `inlineCreationState = Hidden`.
  - Verify the full happy-path flow: add → type name → confirm → loading → success → hidden.
  - Verify error flow: add → type name → confirm → loading → failure → error state with message.
  - Verify cancel from error state returns to Hidden.

- **InlineItemEditor UI tests**:
  - Verify that `BringIntoViewRequester.bringIntoView()` is invoked when the editor receives initial focus (validates Requirement 2.6 — editor visible above keyboard on first appearance).

- **InlineCreationState tests**:
  - Specific edge case: name with leading/trailing whitespace is trimmed before submission.
  - Specific edge case: very long folder name (boundary testing).

- **Grid layout logic tests**:
  - 0 folders → 1 item (AddButton), 1 row with spacer.
  - 1 folder → 2 items, 1 full row.
  - 3 folders → 4 items, 2 full rows.

### Test Configuration

- Property tests: minimum 100 iterations via `checkAll` default or `.config(invocations = ...)`.
- All tests run in `commonTest` using `StandardTestDispatcher` for coroutine control.
- Fake implementations: `FakeFolderRepository` and existing `FakeNotesRepository` for ViewModel tests.
