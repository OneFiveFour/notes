# Implementation Plan: String Resource Extraction

## Overview

Extract all 17 hardcoded user-facing strings from Kotlin source files into `composeResources/values/strings.xml`, replacing them with `stringResource()` calls in composables and `getString()` calls in ViewModels. This enables localization without modifying Kotlin code.

## Tasks

- [ ] 1. Create the default string resource file
  - [ ] 1.1 Create `composeApp/src/commonMain/composeResources/values/strings.xml` with all 17 string entries
    - Include all keys: `app_name`, `login_backend_url_label`, `login_username_label`, `login_password_label`, `login_button`, `error_backend_url_required`, `error_username_required`, `error_password_required`, `error_login_failed`, `edit_note_title_new`, `edit_note_title_edit`, `edit_note_placeholder`, `navigate_back`, `save_button`, `edit_tasklist_title_new`, `edit_tasklist_title_edit`, `edit_tasklist_placeholder`, `home_title`
    - Use the exact English values specified in the design document's Data Models section
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_

- [ ] 2. Extract Login Screen strings
  - [ ] 2.1 Update `LoginScreen.kt` to use `stringResource()` for all 5 hardcoded strings
    - Replace `"EchoList"` with `stringResource(Res.string.app_name)`
    - Replace `"Backend URL"` with `stringResource(Res.string.login_backend_url_label)`
    - Replace `"Username"` with `stringResource(Res.string.login_username_label)`
    - Replace `"Password"` with `stringResource(Res.string.login_password_label)`
    - Replace `"Log in"` with `stringResource(Res.string.login_button)`
    - Add necessary imports for `org.jetbrains.compose.resources.stringResource` and `echolist.composeapp.generated.resources.*`
    - _Requirements: 2.1, 2.2_

  - [ ] 2.2 Update `LoginViewModel.kt` to use `getString()` for all 4 validation error strings
    - Move validation logic into the existing `viewModelScope.launch` coroutine block so `getString()` (suspend) can be called
    - Replace `"Backend URL is required"` with `getString(Res.string.error_backend_url_required)`
    - Replace `"Username is required"` with `getString(Res.string.error_username_required)`
    - Replace `"Password is required"` with `getString(Res.string.error_password_required)`
    - Replace `"Login failed"` with `getString(Res.string.error_login_failed)`
    - Add necessary imports for `org.jetbrains.compose.resources.getString` and `echolist.composeapp.generated.resources.*`
    - _Requirements: 3.1, 3.2_

  - [ ]* 2.3 Write property test for LoginViewModel validation error strings
    - Create `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/login/LoginViewModelStringResourcePropertyTest.kt`
    - **Property 2: Validation errors use resource strings for all input combinations**
    - Generate random combinations of blank/non-blank strings for backend URL, username, and password
    - Assert every non-null error field in the resulting `LoginUiState` matches the corresponding resource string value
    - Use Kotest Property with minimum 100 iterations
    - Tag: `Feature: string-resource-extraction, Property 2: Validation errors use resource strings`
    - **Validates: Requirements 3.2**

- [ ] 3. Checkpoint - Ensure login screen builds and tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Extract Edit Note Screen strings
  - [ ] 4.1 Update `EditNoteScreen.kt` to use `stringResource()` for all 5 hardcoded strings
    - Replace `"New Note"` with `stringResource(Res.string.edit_note_title_new)`
    - Replace `"Edit Note"` with `stringResource(Res.string.edit_note_title_edit)`
    - Replace `"Enter note content"` with `stringResource(Res.string.edit_note_placeholder)`
    - Replace `"Navigate back"` with `stringResource(Res.string.navigate_back)`
    - Replace `"Save"` with `stringResource(Res.string.save_button)`
    - Add necessary imports for `org.jetbrains.compose.resources.stringResource` and `echolist.composeapp.generated.resources.*`
    - _Requirements: 4.1, 4.2_

- [ ] 5. Extract Edit Tasklist Screen strings
  - [ ] 5.1 Update `EditTaskListScreen.kt` to use `stringResource()` for 3 screen-specific strings and reuse 2 shared keys
    - Replace `"New Tasklist"` with `stringResource(Res.string.edit_tasklist_title_new)`
    - Replace `"Edit Tasklist"` with `stringResource(Res.string.edit_tasklist_title_edit)`
    - Replace `"Enter tasklist content"` with `stringResource(Res.string.edit_tasklist_placeholder)`
    - Reuse `stringResource(Res.string.navigate_back)` for the navigate-back icon content description
    - Reuse `stringResource(Res.string.save_button)` for the save button text
    - Add necessary imports for `org.jetbrains.compose.resources.stringResource` and `echolist.composeapp.generated.resources.*`
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ] 6. Extract Home Screen strings
  - [ ] 6.1 Update `titleFromPath()` and `buildBreadcrumbs()` to accept a `homeTitle` parameter
    - Change signature of `titleFromPath(path: String)` to `titleFromPath(path: String, homeTitle: String)`
    - Change signature of `buildBreadcrumbs(path: String)` to `buildBreadcrumbs(path: String, homeTitle: String)`
    - Replace hardcoded `"Home"` with the `homeTitle` parameter in both functions
    - _Requirements: 6.1, 6.2_

  - [ ] 6.2 Update `HomeViewModel` to resolve `home_title` via `getString()` and pass it to `titleFromPath`/`buildBreadcrumbs`
    - Call `getString(Res.string.home_title)` in the `loadData()` suspend function
    - Pass the resolved string to `titleFromPath(path, homeTitle)` and `buildBreadcrumbs(path, homeTitle)`
    - Add necessary imports for `org.jetbrains.compose.resources.getString` and `echolist.composeapp.generated.resources.*`
    - _Requirements: 6.1, 6.2_

  - [ ]* 6.3 Write property test for home title parameterization
    - Create `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/home/HomeTitlePropertyTest.kt`
    - **Property 1: Home title parameterization**
    - Generate random path strings and random non-empty home title strings
    - Assert `titleFromPath(path, homeTitle)` returns `homeTitle` when path is `"/"` or empty
    - Assert `buildBreadcrumbs(path, homeTitle)` always has `homeTitle` as the label of the first breadcrumb item
    - Use Kotest Property with minimum 100 iterations
    - Tag: `Feature: string-resource-extraction, Property 1: Home title parameterization`
    - **Validates: Requirements 6.2**

- [ ] 7. Final checkpoint - Verify no hardcoded UI strings remain
  - Ensure all tests pass, ask the user if questions arise.
  - Verify that no user-facing hardcoded strings remain in composable functions or ViewModel classes
  - _Requirements: 7.1, 7.2_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- The Compose Resources Gradle plugin auto-generates `Res.string.*` from `strings.xml` at build time
