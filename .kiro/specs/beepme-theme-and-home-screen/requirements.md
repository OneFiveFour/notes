# Requirements Document

## Introduction

This document defines the requirements for the EchoList notes app's Material 3 theme system and home screen implementation. The feature covers two areas: (1) a multi-theme infrastructure supporting selectable color themes with light/dark variants, and (2) a home screen UI matching the Figma design, built on top of that theme system. The implementation targets Kotlin Compose Multiplatform (Android and iOS at minimum).

## Glossary

- **Theme_System**: The Compose-based infrastructure that provides color schemes, typography, and shape definitions to the entire application via `MaterialTheme`.
- **Color_Theme**: A named set of color values (e.g. "EchoList Classic") that defines both a light and a dark `ColorScheme`.
- **Theme_Selector**: The mechanism that allows the user to choose which Color_Theme is active.
- **Home_Screen**: The main screen of the app displaying folders and files for the current navigation path.
- **Header**: The top section of the Home_Screen containing a navigation icon and the current folder title.
- **Breadcrumb_Nav**: A horizontal row of path segments showing the navigation hierarchy, with the current segment highlighted in a pill/chip.
- **Folder_Card**: A card component in a 2-column grid representing a folder, showing an icon, folder name, and item count.
- **File_Item**: A list item component representing a note file, showing an icon, title, preview text, and timestamp.
- **FAB**: The floating action button positioned at the bottom-right of the Home_Screen for creating new items.
- **Work_Sans**: The font family used throughout the application for all text rendering.

## Requirements

### Requirement 1: Define Color Theme Data Structure

**User Story:** As a developer, I want a data structure that represents a selectable color theme with light and dark variants, so that the app can support multiple themes.

#### Acceptance Criteria

1. THE Theme_System SHALL define a Color_Theme data structure containing a unique name, a light ColorScheme, and a dark ColorScheme.
2. THE Theme_System SHALL provide a "EchoList Classic" Color_Theme using background #FFFAF0, surface White, primary #023047, and secondary #780000 for the light variant.
3. THE Theme_System SHALL provide a dark variant for the "EchoList Classic" Color_Theme with appropriately adjusted colors for dark mode readability.
4. WHEN a new Color_Theme is added to the Theme_System, THE Theme_System SHALL require only the definition of a name, a light ColorScheme, and a dark ColorScheme with no changes to existing code.

### Requirement 2: Theme Selection and Application

**User Story:** As a user, I want to select a color theme for the app, so that I can personalize the look and feel.

#### Acceptance Criteria

1. THE Theme_Selector SHALL maintain the currently selected Color_Theme as observable state.
2. WHEN the user selects a different Color_Theme, THE Theme_Selector SHALL update the active theme and the UI SHALL recompose with the new colors.
3. THE Theme_System SHALL resolve the active ColorScheme by selecting the light or dark variant of the current Color_Theme based on the system dark mode setting.
4. THE Theme_System SHALL apply the resolved ColorScheme, the Work_Sans typography, and the app shape definitions through Compose `MaterialTheme`.

### Requirement 3: Typography Definition

**User Story:** As a designer, I want all text in the app to use the Work Sans font family with consistent styles, so that the UI matches the design system.

#### Acceptance Criteria

1. THE Theme_System SHALL define a Compose `Typography` using the Work_Sans font family for all text styles.
2. THE Theme_System SHALL map Work_Sans Bold 24sp to `titleLarge`, Work_Sans SemiBold 14sp to `titleSmall`, Work_Sans Medium 14sp to `labelMedium`, Work_Sans Medium 10sp to `labelSmall`, Work_Sans Regular 10sp to `bodySmall`, and Work_Sans Regular 14sp to `bodyMedium`.

### Requirement 4: Shape Definition

**User Story:** As a designer, I want consistent corner rounding across all UI components, so that the app has a cohesive visual style.

#### Acceptance Criteria

1. THE Theme_System SHALL define a Compose `Shapes` object with small corners at 8dp and medium corners at 12dp.
2. THE Home_Screen components SHALL use the medium shape (12dp) for Folder_Card and File_Item containers, and the small shape (8dp) for icon containers.

### Requirement 5: Home Screen Header

**User Story:** As a user, I want to see the current folder name and a navigation icon at the top of the home screen, so that I know where I am and can navigate.

#### Acceptance Criteria

1. THE Header SHALL display a navigation icon on the leading side and the current folder title using `titleLarge` typography.
2. THE Header SHALL use the primary color from the active ColorScheme for the title text and the navigation icon tint.

### Requirement 6: Breadcrumb Navigation

**User Story:** As a user, I want to see a breadcrumb trail of my navigation path, so that I can understand my location in the folder hierarchy and navigate back.

#### Acceptance Criteria

1. THE Breadcrumb_Nav SHALL display all path segments in a horizontal row using `labelMedium` typography.
2. THE Breadcrumb_Nav SHALL render the last path segment inside a pill-shaped chip with the primary color as background and an on-primary color for text.
3. THE Breadcrumb_Nav SHALL render non-current path segments as plain text with the primary color.
4. WHEN a user taps a non-current breadcrumb segment, THE Breadcrumb_Nav SHALL invoke a navigation callback with the tapped segment's path.

### Requirement 7: Folders Grid Section

**User Story:** As a user, I want to see my folders displayed in a grid, so that I can quickly browse and open them.

#### Acceptance Criteria

1. THE Home_Screen SHALL display a "FOLDERS" section label using `labelSmall` typography in the primary color.
2. THE Home_Screen SHALL display Folder_Card items in a 2-column grid layout.
3. EACH Folder_Card SHALL display a 40x40dp icon container with the primary color background and small (8dp) rounded corners, the folder name in Work_Sans Bold 14sp, and the item count in Work_Sans Regular 10sp.
4. EACH Folder_Card SHALL have a white background, medium (12dp) rounded corners, and a 1dp border in the primary color (#023047).
5. WHEN a user taps a Folder_Card, THE Home_Screen SHALL invoke a navigation callback with the tapped folder's identifier.

### Requirement 8: Files List Section

**User Story:** As a user, I want to see my notes listed below the folders, so that I can browse and open them.

#### Acceptance Criteria

1. THE Home_Screen SHALL display a "FILES" section label using `labelSmall` typography in the primary color.
2. THE Home_Screen SHALL display File_Item components in a vertical list.
3. EACH File_Item SHALL display a 36x36dp icon container with a 5% opacity primary color background and small (8dp) rounded corners, the note title in `titleSmall` typography, a content preview in `bodySmall` typography, and a timestamp in `labelSmall` typography.
4. EACH File_Item SHALL have a white background, medium (12dp) rounded corners, and a 1dp border in the secondary color (#780000).
5. WHEN a user taps a File_Item, THE Home_Screen SHALL invoke a callback with the tapped note's identifier.

### Requirement 9: Floating Action Button

**User Story:** As a user, I want a prominent button to create new items, so that I can quickly add notes or folders.

#### Acceptance Criteria

1. THE FAB SHALL be positioned at the bottom-right of the Home_Screen with appropriate padding.
2. THE FAB SHALL have a 64x64dp size, the secondary color (#780000) as background, 16dp rounded corners, a plus icon in white, and an elevation shadow.
3. WHEN a user taps the FAB, THE Home_Screen SHALL invoke a creation callback.

### Requirement 10: Home Screen Composition

**User Story:** As a developer, I want the home screen to compose all sections into a scrollable layout, so that the user can view all content.

#### Acceptance Criteria

1. THE Home_Screen SHALL use the background color from the active ColorScheme as the screen background.
2. THE Home_Screen SHALL arrange the Header, Breadcrumb_Nav, folders grid, and files list in a vertically scrollable layout.
3. THE Home_Screen SHALL overlay the FAB on top of the scrollable content, anchored to the bottom-right.
4. WHEN the folders list is empty, THE Home_Screen SHALL hide the "FOLDERS" section entirely.
5. WHEN the files list is empty, THE Home_Screen SHALL hide the "FILES" section entirely.
