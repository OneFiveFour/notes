# Requirements Document

## Introduction

This feature adds an "add file" button as the last item in the files section of the home screen. Unlike the folder add button (which shows a single plus icon), this button contains two sub-buttons: one to create a new note and one to create a new tasklist. Each sub-button navigates to its respective detail screen. Both the note detail screen and the tasklist detail screen provide a simple editable TextField and a non-functional save button.

## Glossary

- **Home_Screen**: The main screen of EchoList displaying folders and files in the current directory.
- **Files_Section**: The section of the Home_Screen that displays file items in a 2-column grid layout.
- **Add_File_Button**: A composite button rendered as the last item in the Files_Section grid, containing two sub-buttons for creating notes and tasklists.
- **Add_Note_Button**: A sub-button within the Add_File_Button that triggers navigation to the Note_Detail_Screen.
- **Add_Tasklist_Button**: A sub-button within the Add_File_Button that triggers navigation to the Tasklist_Detail_Screen.
- **Note_Detail_Screen**: A screen for creating a new note, containing an editable text field and a save button.
- **Tasklist_Detail_Screen**: A screen for creating a new tasklist, containing an editable text field and a save button.
- **Navigator**: The component responsible for screen transitions within the app.

## Requirements

### Requirement 1: Add File Button Placement

**User Story:** As a user, I want to see an add button at the end of the files section, so that I can create new files from the home screen.

#### Acceptance Criteria

1. THE Home_Screen SHALL display the Add_File_Button as the last item in the Files_Section 2-column grid.
2. THE Add_File_Button SHALL use secondary-colored borders consistent with other file items in the Files_Section.
3. THE Add_File_Button SHALL use the same medium shape and surface background as other file items.

### Requirement 2: Add File Button Content

**User Story:** As a user, I want the add file button to offer two distinct creation options, so that I can choose between creating a note or a tasklist.

#### Acceptance Criteria

1. THE Add_File_Button SHALL contain exactly two sub-buttons: the Add_Note_Button and the Add_Tasklist_Button.
2. THE Add_Note_Button SHALL display a label or icon that identifies the note creation action.
3. THE Add_Tasklist_Button SHALL display a label or icon that identifies the tasklist creation action.
4. THE Add_File_Button SHALL arrange the Add_Note_Button and the Add_Tasklist_Button so that both are visible and tappable without overlap.

### Requirement 3: Navigation to Note Detail Screen

**User Story:** As a user, I want to navigate to a note creation screen when I tap the add-note button, so that I can start writing a new note.

#### Acceptance Criteria

1. WHEN the user taps the Add_Note_Button, THE Navigator SHALL navigate to the Note_Detail_Screen.
2. THE Note_Detail_Screen SHALL display an editable TextField for entering note content.
3. THE Note_Detail_Screen SHALL display a save button.
4. WHEN the user taps the save button on the Note_Detail_Screen, THE Note_Detail_Screen SHALL perform no action.
5. THE Note_Detail_Screen SHALL display a back navigation button in the top app bar.
6. WHEN the user taps the back button on the Note_Detail_Screen, THE Navigator SHALL return to the Home_Screen.

### Requirement 4: Navigation to Tasklist Detail Screen

**User Story:** As a user, I want to navigate to a tasklist creation screen when I tap the add-tasklist button, so that I can start creating a new tasklist.

#### Acceptance Criteria

1. WHEN the user taps the Add_Tasklist_Button, THE Navigator SHALL navigate to the Tasklist_Detail_Screen.
2. THE Tasklist_Detail_Screen SHALL display an editable TextField for entering tasklist content.
3. THE Tasklist_Detail_Screen SHALL display a save button.
4. WHEN the user taps the save button on the Tasklist_Detail_Screen, THE Tasklist_Detail_Screen SHALL perform no action.
5. THE Tasklist_Detail_Screen SHALL display a back navigation button in the top app bar.
6. WHEN the user taps the back button on the Tasklist_Detail_Screen, THE Navigator SHALL return to the Home_Screen.

### Requirement 5: Design System Compliance

**User Story:** As a developer, I want all new composables to follow the EchoList design system, so that the UI remains consistent.

#### Acceptance Criteria

1. THE Add_File_Button SHALL use MaterialTheme.colorScheme for all color references.
2. THE Add_File_Button SHALL use MaterialTheme.typography for all text styles.
3. THE Add_File_Button SHALL use LocalEchoListDimensions for spacing and sizing.
4. THE Note_Detail_Screen SHALL use MaterialTheme.colorScheme, MaterialTheme.typography, and LocalEchoListDimensions for all styling.
5. THE Tasklist_Detail_Screen SHALL use MaterialTheme.colorScheme, MaterialTheme.typography, and LocalEchoListDimensions for all styling.
6. THE Add_File_Button composable SHALL be stateless, receiving UI state and emitting callbacks.
7. THE Note_Detail_Screen composable SHALL be stateless, receiving UI state and emitting callbacks.
8. THE Tasklist_Detail_Screen composable SHALL be stateless, receiving UI state and emitting callbacks.
