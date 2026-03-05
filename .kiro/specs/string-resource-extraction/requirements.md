# Requirements Document

## Introduction

EchoList is a Compose Multiplatform application targeting Android, iOS, JVM, JS, and WasmJS. All user-facing strings are currently hardcoded in Kotlin source files. This feature extracts those strings into Compose Multiplatform string resources (`composeResources/values/strings.xml`) so the app can be localized into multiple languages. The default language is English.

## Glossary

- **App**: The EchoList Compose Multiplatform application
- **String_Resource_File**: The XML file at `composeResources/values/strings.xml` that holds all translatable string definitions for the default locale
- **Resource_Reference**: A call to `stringResource(Res.string.<key>)` or `org.jetbrains.compose.resources.getString(Res.string.<key>)` that resolves a string at runtime from the String_Resource_File
- **Composable**: A Kotlin function annotated with `@Composable` that defines UI
- **ViewModel**: A Kotlin class extending `androidx.lifecycle.ViewModel` that holds UI state and business logic
- **Hardcoded_String**: A string literal in source code that is displayed to the user

## Requirements

### Requirement 1: Default String Resource File

**User Story:** As a developer, I want all translatable strings defined in a single resource file, so that translators can work with one file per locale.

#### Acceptance Criteria

1. THE App SHALL contain a String_Resource_File at `composeResources/values/strings.xml` with all user-facing strings defined as `<string name="key">value</string>` entries
2. WHEN a new locale is added, THE App SHALL resolve strings from a locale-specific file at `composeResources/values-<locale>/strings.xml` when available, falling back to the default String_Resource_File

### Requirement 2: Extract Login Screen Strings

**User Story:** As a developer, I want all hardcoded strings in the login screen extracted into resource references, so that the login screen can be localized.

#### Acceptance Criteria

1. THE App SHALL define the following string resources in the String_Resource_File: `app_name` ("EchoList"), `login_backend_url_label` ("Backend URL"), `login_username_label` ("Username"), `login_password_label` ("Password"), `login_button` ("Log in")
2. WHEN the LoginScreen Composable renders, THE App SHALL use Resource_References for the app title, field labels, and button text instead of Hardcoded_Strings

### Requirement 3: Extract Login Validation Error Strings

**User Story:** As a developer, I want login validation error messages extracted into resource references, so that error messages can be localized.

#### Acceptance Criteria

1. THE App SHALL define the following string resources in the String_Resource_File: `error_backend_url_required` ("Backend URL is required"), `error_username_required` ("Username is required"), `error_password_required` ("Password is required"), `error_login_failed` ("Login failed")
2. WHEN the LoginViewModel produces a validation error, THE LoginViewModel SHALL use Resource_References for all error messages instead of Hardcoded_Strings

### Requirement 4: Extract Edit Note Screen Strings

**User Story:** As a developer, I want all hardcoded strings in the edit note screen extracted into resource references, so that the edit note screen can be localized.

#### Acceptance Criteria

1. THE App SHALL define the following string resources in the String_Resource_File: `edit_note_title_new` ("New Note"), `edit_note_title_edit` ("Edit Note"), `edit_note_placeholder` ("Enter note content"), `navigate_back` ("Navigate back"), `save_button` ("Save")
2. WHEN the EditNoteScreen Composable renders, THE App SHALL use Resource_References for the screen title, placeholder text, content descriptions, and button text instead of Hardcoded_Strings

### Requirement 5: Extract Edit Tasklist Screen Strings

**User Story:** As a developer, I want all hardcoded strings in the edit tasklist screen extracted into resource references, so that the edit tasklist screen can be localized.

#### Acceptance Criteria

1. THE App SHALL define the following string resources in the String_Resource_File: `edit_tasklist_title_new` ("New Tasklist"), `edit_tasklist_title_edit` ("Edit Tasklist"), `edit_tasklist_placeholder` ("Enter tasklist content")
2. WHEN the EditTaskListScreen Composable renders, THE App SHALL use Resource_References for the screen title, placeholder text, content descriptions, and button text instead of Hardcoded_Strings
3. WHEN the EditTaskListScreen Composable renders the navigate-back icon, THE App SHALL reuse the `navigate_back` string resource defined in Requirement 4
4. WHEN the EditTaskListScreen Composable renders the save button, THE App SHALL reuse the `save_button` string resource defined in Requirement 4

### Requirement 6: Extract Home Screen Strings

**User Story:** As a developer, I want the hardcoded "Home" string in the home screen extracted into a resource reference, so that the home screen can be localized.

#### Acceptance Criteria

1. THE App SHALL define the following string resource in the String_Resource_File: `home_title` ("Home")
2. WHEN the HomeViewModel computes the title or breadcrumb label for the root path, THE HomeViewModel SHALL use a Resource_Reference for the "Home" string instead of a Hardcoded_String

### Requirement 7: No Remaining Hardcoded UI Strings

**User Story:** As a developer, I want to ensure no user-facing hardcoded strings remain in UI code, so that the app is fully prepared for localization.

#### Acceptance Criteria

1. THE App SHALL contain zero Hardcoded_Strings in Composable functions that are displayed to the user
2. THE App SHALL contain zero Hardcoded_Strings in ViewModel classes that are displayed to the user via UI state
