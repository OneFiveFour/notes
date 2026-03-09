# Requirements Document

## Introduction

The Inline Item Creation feature transforms the existing `CreateItemPills` composable from a simple row of clickable pills into an inline creation flow. When a user clicks a pill (Note, Task, or Folder), the other pills animate away and the selected pill expands into a text input field. The user can type a title and confirm via the keyboard IME action. The close button (RoundIconButton) remains visible at all times and resets the UI back to the default three-pill state.

## Glossary

- **CreateItemPills**: The composable that displays a horizontal row of item-type pills (Note, Task, Folder) and a close button
- **Pill**: A rounded, colored button representing an item type (Note, Task, or Folder)
- **TextField**: A single-line Material 3 text input field for entering the title of a new item
- **RoundIconButton**: The circular icon button displayed at the end of the pill row, used to close/reset the pills
- **IME_Action**: The keyboard action button (e.g., Done) used to confirm text input
- **Idle_State**: The default state where all three pills are visible
- **Input_State**: The state where a single pill has expanded into a TextField for title entry
- **AnimatedContent**: A Compose animation container that animates between different content based on a target state

## Requirements

### Requirement 1: Pill Selection Triggers Input Mode

**User Story:** As a user, I want to click a pill to start creating an item of that type inline, so that I can quickly name new items without navigating away.

#### Acceptance Criteria

1. WHEN a user clicks the Note pill, THE CreateItemPills SHALL transition from Idle_State to Input_State with the Note item type selected
2. WHEN a user clicks the Task pill, THE CreateItemPills SHALL transition from Idle_State to Input_State with the Task item type selected
3. WHEN a user clicks the Folder pill, THE CreateItemPills SHALL transition from Idle_State to Input_State with the Folder item type selected

### Requirement 2: Animated Transition Between States

**User Story:** As a user, I want smooth animations when switching between the pill row and the text input, so that the UI feels polished and responsive.

#### Acceptance Criteria

1. WHEN the CreateItemPills transitions from Idle_State to Input_State, THE CreateItemPills SHALL animate the disappearance of the non-selected pills and the expansion of the selected pill using AnimatedContent or Crossfade
2. WHEN the CreateItemPills transitions from Input_State to Idle_State, THE CreateItemPills SHALL animate the reappearance of all three pills using AnimatedContent or Crossfade

### Requirement 3: Expanded Pill Shows TextField

**User Story:** As a user, I want the selected pill to expand into a text field, so that I can type the title of the new item directly.

#### Acceptance Criteria

1. WHILE in Input_State, THE CreateItemPills SHALL display a single-line TextField in place of the three pills
2. WHILE in Input_State, THE TextField SHALL fill the available width up to the RoundIconButton
3. WHILE in Input_State, THE TextField SHALL use the color of the selected pill type as its background color, matching the pill design style (rounded shape, same padding)
4. WHEN transitioning to Input_State, THE TextField SHALL request keyboard focus immediately so the software keyboard appears

### Requirement 4: IME Action Confirms Creation

**User Story:** As a user, I want to confirm the item title using the keyboard action, so that I can create items efficiently without extra taps.

#### Acceptance Criteria

1. WHEN the user triggers the IME_Action on the TextField, THE CreateItemPills SHALL invoke the corresponding creation callback (onCreateNote, onTaskCreate, or onFolderCreate) with the entered title as a String parameter
2. WHEN the user triggers the IME_Action on the TextField, THE CreateItemPills SHALL transition from Input_State back to Idle_State
3. WHEN the user triggers the IME_Action with an empty title, THE CreateItemPills SHALL transition back to Idle_State without invoking the creation callback

### Requirement 5: Close Button Behavior

**User Story:** As a user, I want the close button to always be visible and to cancel the inline creation, so that I can back out at any time.

#### Acceptance Criteria

1. WHILE in Idle_State, THE RoundIconButton SHALL remain visible at the end of the pill row
2. WHILE in Input_State, THE RoundIconButton SHALL remain visible at the end of the TextField
3. WHEN the user clicks the RoundIconButton while in Input_State, THE CreateItemPills SHALL transition from Input_State to Idle_State
4. WHEN the user clicks the RoundIconButton while in Input_State, THE CreateItemPills SHALL invoke the onClosePills callback
5. WHEN the user clicks the RoundIconButton while in Idle_State, THE CreateItemPills SHALL invoke the onClosePills callback

### Requirement 6: Stateless Composable Contract

**User Story:** As a developer, I want the CreateItemPills composable to remain stateless, so that it follows the existing project architecture patterns.

#### Acceptance Criteria

1. THE CreateItemPills SHALL receive all UI state as parameters and emit user actions as callbacks
2. THE CreateItemPills SHALL accept separate creation callbacks per item type (onCreateNote, onTaskCreate, onFolderCreate), each accepting a String parameter for the entered title
3. THE CreateItemPills SHALL manage local animation state and text field state within its composition scope using Compose remember/mutableStateOf, consistent with the pattern used by BottomNavigation
