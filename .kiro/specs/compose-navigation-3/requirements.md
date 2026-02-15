# Requirements Document

## Introduction

This feature integrates Compose Navigation 3 into the EchoList Kotlin Multiplatform project, replacing the current hardcoded single-screen rendering in `App.kt` with a proper navigation system. Navigation 3 provides a user-owned back stack and low-level Compose building blocks for managing screen transitions across all target platforms (Android, iOS, JVM, JS, WasmJS). The integration establishes the navigation foundation with a Home destination and a Note Detail destination, enabling future screens to be added with minimal effort.

Note: Navigation 3 is currently in alpha (1.0.0-alpha05). The API surface may change in future releases.

## Glossary

- **Navigation_3**: The Compose Navigation 3 library (`org.jetbrains.androidx.navigation3:navigation3-ui`) providing back stack management and destination rendering for Compose Multiplatform.
- **NavKey**: A serializable marker interface from Navigation 3 that all route definitions must implement to identify destinations.
- **NavDisplay**: The Navigation 3 composable that observes a back stack and renders the corresponding destination content.
- **Back_Stack**: A `SnapshotStateList` of `NavKey` instances owned and managed by the application, representing the current navigation history.
- **Route**: A data class or data object implementing `NavKey` that uniquely identifies a navigation destination and carries any required parameters.
- **Home_Destination**: The screen displaying folders and files for a given path, currently implemented as `HomeScreen`.
- **Note_Detail_Destination**: A new screen displaying the full content of a single note.
- **SavedStateConfiguration**: A Navigation 3 configuration object that registers polymorphic serializers for `NavKey` subtypes, required for state restoration on non-JVM platforms.
- **EchoList_App**: The root composable (`App.kt`) that hosts the theme and navigation infrastructure.

## Requirements

### Requirement 1: Build Configuration

**User Story:** As a developer, I want the necessary Navigation 3 and serialization dependencies added to the project, so that the navigation APIs are available across all platforms.

#### Acceptance Criteria

1. THE Build_System SHALL include the `kotlinx-serialization` Gradle plugin in the project build configuration.
2. THE Build_System SHALL declare Navigation 3 library dependencies (`navigation3-ui`) in the version catalog and apply them to the `commonMain` source set.
3. THE Build_System SHALL declare the `lifecycle-viewmodel-navigation3` dependency in the version catalog and apply it to the `commonMain` source set.
4. WHEN the project is compiled for any target platform (Android, iOS, JVM, JS, WasmJS), THE Build_System SHALL resolve all Navigation 3 and serialization dependencies without errors.

### Requirement 2: Route Definitions

**User Story:** As a developer, I want type-safe route definitions for each destination, so that navigation targets are clearly defined and compile-time checked.

#### Acceptance Criteria

1. THE Route_Registry SHALL define a `HomeRoute` data class implementing `NavKey` with a `path` parameter defaulting to the root path.
2. THE Route_Registry SHALL define a `NoteDetailRoute` data class implementing `NavKey` with a `noteId` parameter of type `String`.
3. THE Route_Registry SHALL annotate all route classes with `@Serializable` for kotlinx-serialization support.
4. THE Route_Registry SHALL register all `NavKey` subtypes in a polymorphic `SerializersModule` within a `SavedStateConfiguration` for cross-platform state restoration.

### Requirement 3: Navigation Host Setup

**User Story:** As a developer, I want a central navigation host in the app root, so that destinations are rendered based on the current back stack state.

#### Acceptance Criteria

1. WHEN the EchoList_App starts, THE EchoList_App SHALL initialize a `Back_Stack` with `HomeRoute` as the initial destination.
2. THE EchoList_App SHALL render a `NavDisplay` composable that observes the `Back_Stack` and displays the corresponding destination content.
3. THE EchoList_App SHALL provide a `SavedStateConfiguration` with all registered `NavKey` serializers to the `Back_Stack` for cross-platform state persistence.
4. WHEN the `Back_Stack` contains a `HomeRoute`, THE NavDisplay SHALL render the Home_Destination screen.
5. WHEN the `Back_Stack` contains a `NoteDetailRoute`, THE NavDisplay SHALL render the Note_Detail_Destination screen.

### Requirement 4: Forward Navigation

**User Story:** As a user, I want to navigate from the home screen to subfolders and note details, so that I can browse my notes hierarchy and read individual notes.

#### Acceptance Criteria

1. WHEN a user taps a folder on the Home_Destination, THE EchoList_App SHALL push a new `HomeRoute` with the selected folder's path onto the `Back_Stack`.
2. WHEN a user taps a file on the Home_Destination, THE EchoList_App SHALL push a `NoteDetailRoute` with the selected file's `noteId` onto the `Back_Stack`.
3. WHEN a user taps a breadcrumb on the Home_Destination, THE EchoList_App SHALL navigate to the `HomeRoute` corresponding to the selected breadcrumb path by removing all entries above it from the `Back_Stack`.

### Requirement 5: Back Navigation

**User Story:** As a user, I want to navigate back to the previous screen, so that I can retrace my steps through the app.

#### Acceptance Criteria

1. WHEN the user triggers a back action and the `Back_Stack` contains more than one entry, THE EchoList_App SHALL remove the top entry from the `Back_Stack`, revealing the previous destination.
2. WHEN the user taps the back arrow on the Home_Destination and the `Back_Stack` contains more than one entry, THE EchoList_App SHALL remove the top entry from the `Back_Stack`.
3. WHEN the `Back_Stack` contains only the initial `HomeRoute`, THE EchoList_App SHALL not remove the last entry from the `Back_Stack`.

### Requirement 6: Note Detail Screen

**User Story:** As a user, I want to view the full content of a note, so that I can read my notes in detail.

#### Acceptance Criteria

1. WHEN the Note_Detail_Destination is displayed, THE Note_Detail_Destination SHALL show the note title, full content, and last-updated timestamp.
2. WHEN the Note_Detail_Destination is displayed, THE Note_Detail_Destination SHALL provide a back navigation action in the top app bar.
3. IF the note identified by `noteId` cannot be found, THEN THE Note_Detail_Destination SHALL display an error message indicating the note is unavailable.

### Requirement 7: Dependency Injection Integration

**User Story:** As a developer, I want navigation-related components registered in the DI container, so that ViewModels and other dependencies are consistently provided.

#### Acceptance Criteria

1. THE DI_Container SHALL provide a `HomeViewModel` that loads folder and file data for a given path and exposes `HomeScreenUiState`.
2. THE DI_Container SHALL provide a `NoteDetailViewModel` that loads note data for a given `noteId` and exposes note detail UI state.
3. WHEN a destination is rendered by the NavDisplay, THE DI_Container SHALL supply the corresponding ViewModel to the destination composable.

### Requirement 8: Stateless Composable Pattern Preservation

**User Story:** As a developer, I want screen composables to remain stateless, so that the existing architecture pattern is preserved and screens remain testable.

#### Acceptance Criteria

1. THE Home_Destination composable SHALL receive UI state and navigation callbacks as parameters without directly accessing the `Back_Stack` or ViewModel.
2. THE Note_Detail_Destination composable SHALL receive UI state and navigation callbacks as parameters without directly accessing the `Back_Stack` or ViewModel.
3. WHEN wiring destinations in the NavDisplay, THE EchoList_App SHALL create ViewModels and connect callbacks at the NavDisplay entry-point level, keeping screen composables stateless.
