# Requirements Document

## Introduction

This feature adds inline item creation to the EchoList HomeScreen. The first use case is creating new folders directly within the folder grid. The pattern is designed for reuse so that note file creation can follow the same approach later. The user taps an "add" button in the grid, the grid is replaced by an inline editor card, and after confirming a name the system creates the item via the network and refreshes the UI.

## Glossary

- **HomeScreen**: The main screen composable that displays folders and files for the current path.
- **HomeViewModel**: The ViewModel that manages HomeScreen UI state and orchestrates data operations.
- **FolderGrid**: The 2-column grid layout within HomeScreen that displays FolderCard components.
- **AddButton**: An outlined button with a plus icon placed as the last item in the FolderGrid, matching the shape and border style of a FolderCard.
- **InlineEditor**: A card-styled text input that replaces the FolderGrid, allowing the user to type a name for a new item. It is styled to match the FolderCard appearance.
- **FolderRepository**: The repository interface responsible for creating, renaming, and deleting folders via the network.
- **CreateFolderParams**: The data model holding domain, parentPath, and name for a folder creation request.
- **UiState**: The `HomeScreenUiState` data class that drives the HomeScreen composable.

## Requirements

### Requirement 1: Display Add Folder Button

**User Story:** As a user, I want to see an "add folder" button at the end of the folder grid, so that I can initiate folder creation without navigating away.

#### Acceptance Criteria

1. THE HomeScreen SHALL display the AddButton as the last item in the FolderGrid after all existing FolderCard components.
2. THE AddButton SHALL use the same medium shape and primary-colored border as a FolderCard.
3. THE AddButton SHALL display a plus icon centered within the card area.
4. WHEN the FolderGrid contains an odd number of items including the AddButton, THE HomeScreen SHALL fill the remaining grid cell with empty space to maintain the 2-column layout.

### Requirement 2: Inline Folder Name Editor

**User Story:** As a user, I want to type a new folder name inline without leaving the current screen, so that folder creation feels fast and seamless.

#### Acceptance Criteria

1. WHEN the user taps the AddButton, THE HomeScreen SHALL replace the FolderGrid with the InlineEditor.
2. THE InlineEditor SHALL display a text input field styled to match the FolderCard appearance, using the same medium shape, primary border, and surface background.
3. WHEN the InlineEditor is displayed, THE InlineEditor SHALL automatically request keyboard focus on the text input field.
4. THE InlineEditor SHALL display a confirm action and a cancel action.
5. WHEN the user activates the cancel action, THE HomeScreen SHALL hide the InlineEditor and restore the FolderGrid to its previous state without creating a folder.
6. WHEN the InlineEditor appears and the on-screen keyboard opens, THE HomeScreen SHALL ensure the InlineEditor is scrolled into view so it is not obscured by the keyboard. IF the user subsequently scrolls the InlineEditor out of view, THEN the system SHALL NOT attempt to re-scroll it into view.

### Requirement 3: Folder Name Validation

**User Story:** As a user, I want the system to prevent me from creating folders with invalid names, so that I avoid errors.

#### Acceptance Criteria

1. WHEN the user attempts to confirm a folder name that is empty or composed entirely of whitespace, THE InlineEditor SHALL prevent submission and keep the editor open.
2. WHEN the user provides a valid non-blank folder name and confirms, THE HomeViewModel SHALL accept the name for folder creation.

### Requirement 4: Network Folder Creation

**User Story:** As a user, I want the app to create the folder on the server and update the UI, so that my new folder is persisted and visible.

#### Acceptance Criteria

1. WHEN the user confirms a valid folder name, THE HomeViewModel SHALL call FolderRepository.createFolder with the correct CreateFolderParams containing the current domain, parent path, and folder name.
2. WHILE the folder creation network call is in progress, THE HomeScreen SHALL indicate a loading state to the user.
3. WHEN the folder creation network call succeeds, THE HomeViewModel SHALL reload the folder list and update the UiState so the new folder appears in the FolderGrid.
4. WHEN the folder creation network call succeeds, THE HomeScreen SHALL hide the InlineEditor and restore the FolderGrid.
5. IF the folder creation network call fails, THEN THE HomeViewModel SHALL update the UiState with an error message and keep the InlineEditor open so the user can retry or cancel.

### Requirement 5: Reusable Inline Creation Pattern

**User Story:** As a developer, I want the inline creation UI and state management to be reusable, so that the same pattern can be applied to note file creation later.

#### Acceptance Criteria

1. THE InlineEditor composable SHALL accept item-type-agnostic parameters (placeholder text, icon, confirm callback, cancel callback) so it can be reused for different item types.
2. THE HomeViewModel SHALL manage inline creation state through a generic mechanism that can be extended to support note creation without duplicating logic.
3. WHEN the InlineEditor is used for folder creation, THE InlineEditor SHALL display a folder icon. WHERE the InlineEditor is used for other item types, THE InlineEditor SHALL display the icon provided by the caller.
