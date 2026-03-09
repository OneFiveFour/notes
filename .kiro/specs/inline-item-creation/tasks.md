# Implementation Plan: Inline Item Creation

## Overview

Transform `CreateItemPills` from a simple pill row into an inline creation flow with animated transitions, a text field for title entry, and IME action confirmation. The implementation proceeds bottom-up: define the state model and pure logic, update the composable, then propagate callback signature changes through `BottomNavigation` and `HomeScreen`.

## Tasks

- [ ] 1. Define PillsUiState and pure state machine logic
  - [ ] 1.1 Create `PillsUiState` sealed interface and `PillsAction` sealed interface in a new file `CreateItemPillsState.kt`
    - Define `PillsUiState.Idle` and `PillsUiState.Input(itemType: ItemType)`
    - Define `PillsAction` variants: `PillClicked(itemType)`, `ImeConfirm(title: String)`, `CloseClicked`
    - Implement pure `nextState(current: PillsUiState, action: PillsAction): PillsUiState` function
    - Implement pure `resolveImeAction(itemType: ItemType, title: String): ItemType?` function (returns null for empty/blank titles)
    - _Requirements: 1.1, 1.2, 1.3, 4.1, 4.2, 4.3, 5.3_

  - [ ] 1.2 Add `ItemType.pillColor()` and `ItemType.pillLabel()` extension functions in `CreateItemPillsState.kt`
    - `pillColor()` returns the matching color from `EchoListTheme.echoListColorScheme`
    - `pillLabel()` returns "Note", "Task", or "Folder" (empty string for UNSPECIFIED)
    - _Requirements: 3.3_

  - [ ] 1.3 Write property test: Pill selection transitions to correct Input state
    - **Property 1: Pill selection transitions to correct Input state**
    - **Validates: Requirements 1.1, 1.2, 1.3**
    - For any item type in {NOTE, TASK_LIST, FOLDER}, `nextState(Idle, PillClicked(itemType))` should return `Input(itemType)`

  - [ ] 1.4 Write property test: IME confirm with non-empty title invokes correct callback and resets
    - **Property 3: IME confirm with non-empty title invokes correct callback and resets**
    - **Validates: Requirements 4.1, 4.2**
    - For any item type and any non-empty string, `resolveImeAction(itemType, title)` returns that item type, and `nextState(Input(itemType), ImeConfirm(title))` returns `Idle`

  - [ ] 1.5 Write property test: IME confirm with empty title resets without callback
    - **Property 4: IME confirm with empty title resets without callback**
    - **Validates: Requirements 4.3**
    - For any item type, `resolveImeAction(itemType, "")` returns null, and `nextState(Input(itemType), ImeConfirm(""))` returns `Idle`

  - [ ] 1.6 Write property test: Close button in Input state resets and invokes onClosePills
    - **Property 5: Close button in Input state resets and invokes onClosePills**
    - **Validates: Requirements 5.3, 5.4**
    - For any item type, `nextState(Input(itemType), CloseClicked)` returns `Idle`

- [ ] 2. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 3. Rewrite `CreateItemPills` composable with AnimatedContent and inline text field
  - [ ] 3.1 Update `CreateItemPills` callback signatures from `() -> Unit` to `(String) -> Unit`
    - Change `onCreateNote`, `onTaskCreate`, `onFolderCreate` parameter types
    - _Requirements: 6.2_

  - [ ] 3.2 Implement internal state management and AnimatedContent layout
    - Add `pillsState` via `remember { mutableStateOf(PillsUiState.Idle) }` and `textFieldValue` via `remember { mutableStateOf("") }`
    - Restructure layout: outer `Row` with `AnimatedContent(modifier = Modifier.weight(1f))` + `Spacer` + `RoundIconButton` outside animation scope
    - Use `fadeIn`/`expandHorizontally` + `fadeOut`/`shrinkHorizontally` transition spec
    - In `Idle` state: render three `CreateItemPill` composables with click handlers that set `pillsState = Input(itemType)` and clear text
    - In `Input` state: render `BasicTextField` styled as pill (background = `itemType.pillColor()`, shape = `RoundedCornerShape(50)`, padding = horizontal `m` / vertical `s`, text style = `labelMedium`, single line, IME action `Done`)
    - On IME action: use `resolveImeAction` to determine callback, invoke if non-null, reset to `Idle`
    - On close button: reset to `Idle`, invoke `onClosePills`
    - Request focus via `FocusRequester` + `LaunchedEffect` when entering `Input` state
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.3_

  - [ ] 3.3 Write property test: Item type color mapping is consistent
    - **Property 2: Item type color mapping is consistent**
    - **Validates: Requirements 3.3**
    - For any item type in {NOTE, TASK_LIST, FOLDER}, `pillColor()` returns the matching color from `EchoListColorScheme`, and no two item types share a color

- [ ] 4. Propagate callback signature changes through BottomNavigation and HomeScreen
  - [ ] 4.1 Update `BottomNavigation` to accept and pass through `(String) -> Unit` callbacks
    - Change `onNoteCreate`, `onTaskCreate`, `onFolderCreate` parameter types from `() -> Unit` to `(String) -> Unit`
    - Update lambda bodies to forward the title string: `onNoteCreate(title)` etc.
    - _Requirements: 6.2_

  - [ ] 4.2 Update `HomeScreen` to accept and pass through `(String) -> Unit` callbacks
    - Change `onNoteCreate`, `onTaskCreate`, `onFolderCreate` parameter types from `() -> Unit` to `(String) -> Unit`
    - Update preview if needed
    - _Requirements: 6.2_

- [ ] 5. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- The design uses Kotlin with Compose Multiplatform — no language selection needed
- State machine logic is extracted into pure functions for testability without Compose runtime
- Property tests follow existing Kotest `FunSpec` + `checkAll` conventions (see `HomeTitlePropertyTest`)
- Test files go in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/home/`
- `CreateItemPill` and `RoundIconButton` remain unchanged
