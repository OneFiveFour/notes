# Requirements Document

## Introduction

This feature adds folder creation capability to the EchoList home screen. When a user taps the "Folder" create-item pill, a modal dialog appears prompting for a folder name. On confirmation, the folder is created in the current directory via the existing `FileRepository`. The feature also includes a refactoring of the `HomeViewModel` and `HomeScreen` composable to improve separation of concerns and prepare the codebase for upcoming note and task list creation features.

## Glossary

- **Create_Folder_Dialog**: A modal dialog composable that collects a folder name from the user and provides confirm/cancel actions.
- **HomeScreen**: The main composable displaying the folder browser, file list, breadcrumbs, and bottom navigation.
- **HomeViewModel**: The ViewModel responsible for managing the home screen UI state and coordinating data operations.
- **CreateFolderViewModel**: A dedicated ViewModel responsible for managing the create-folder dialog state and executing the folder creation operation.
- **FileRepository**: The domain-layer interface providing file and folder CRUD operations against the backend.
- **CreateFolderParams**: A data transfer object containing `parentDir` and `name` fields required to create a folder.
- **CreateItemPill**: A pill-shaped button in the bottom navigation that triggers item creation for a specific `ItemType`.
- **Current_Directory**: The folder path currently displayed by the HomeScreen, as determined by the active `HomeRoute.path`.
- **Folder_Name**: A non-blank string entered by the user to name the new folder.

## Requirements

### Requirement 1: Show Create Folder Dialog

**User Story:** As a user, I want a dialog to appear when I tap the folder creation pill, so that I can enter a name for the new folder.

#### Acceptance Criteria

1. WHEN the user taps the CreateItemPill of ItemType.FOLDER, THE HomeScreen SHALL display the Create_Folder_Dialog as a modal overlay.
2. THE Create_Folder_Dialog SHALL contain a text input field for the Folder_Name, a confirm button, and a cancel button.
3. THE Create_Folder_Dialog SHALL display the text input field with focus and the keyboard open when the dialog appears.
4. WHEN the Create_Folder_Dialog is displayed, THE confirm button SHALL be disabled while the Folder_Name text field is blank.
5. WHEN the user taps the cancel button or taps outside the dialog, THE Create_Folder_Dialog SHALL close without creating a folder.

### Requirement 2: Create Folder on Confirmation

**User Story:** As a user, I want to confirm the folder name and have the folder created in my current directory, so that I can organize my content.

#### Acceptance Criteria

1. WHEN the user taps the confirm button with a non-blank Folder_Name, THE CreateFolderViewModel SHALL call FileRepository.createFolder with a CreateFolderParams containing the Current_Directory path and the entered Folder_Name.
2. WHEN FileRepository.createFolder returns successfully, THE Create_Folder_Dialog SHALL close and THE HomeScreen SHALL refresh the file list to include the newly created folder.
3. IF FileRepository.createFolder returns a failure, THEN THE Create_Folder_Dialog SHALL display the error message to the user and remain open.
4. WHILE a folder creation request is in progress, THE Create_Folder_Dialog SHALL disable the confirm button and show a loading indicator.

### Requirement 3: Folder Name Validation

**User Story:** As a user, I want immediate feedback on invalid folder names, so that I avoid creating folders with problematic names.

#### Acceptance Criteria

1. THE Create_Folder_Dialog SHALL trim leading and trailing whitespace from the Folder_Name before submission.
2. WHEN the Folder_Name contains only whitespace, THE Create_Folder_Dialog SHALL keep the confirm button disabled.

### Requirement 4: Extract CreateFolderViewModel

**User Story:** As a developer, I want folder creation logic in a dedicated ViewModel, so that the HomeViewModel stays focused on browsing concerns and the codebase is ready for similar note/task list creation features.

#### Acceptance Criteria

1. THE CreateFolderViewModel SHALL manage the dialog visibility state, the Folder_Name input state, the loading state, and the error state independently from the HomeViewModel.
2. THE CreateFolderViewModel SHALL expose a StateFlow of its UI state for the Create_Folder_Dialog composable to observe.
3. THE CreateFolderViewModel SHALL accept the Current_Directory path and the FileRepository as constructor parameters via Koin dependency injection.
4. THE HomeViewModel SHALL remain responsible only for loading and exposing the file list, breadcrumbs, loading state, and error state for the HomeScreen.
5. WHEN a folder is created successfully, THE CreateFolderViewModel SHALL signal the HomeViewModel to refresh the file list.

### Requirement 5: Simplify HomeScreen Composable Parameters

**User Story:** As a developer, I want the HomeScreen composable to have a clean parameter surface, so that it is maintainable as more creation features are added.

#### Acceptance Criteria

1. THE HomeScreen SHALL receive folder-creation callbacks as a single grouped callback object or lambda instead of individual `onCreateFolder`, `onCreateNote`, and `onCreateTaskList` parameters.
2. THE HomeScreen SHALL wire the Create_Folder_Dialog composable internally, observing the CreateFolderViewModel state, so that callers in App.kt do not need to manage dialog state.
3. THE App.kt entry for HomeRoute SHALL provide the CreateFolderViewModel to the HomeScreen via Koin, reducing the number of parameters passed from the navigation layer.
