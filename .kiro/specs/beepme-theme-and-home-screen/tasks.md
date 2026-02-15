# Implementation Plan: BeepMe Theme and Home Screen

## Overview

Implement the Material 3 theme system and home screen in incremental steps. Start with the theme infrastructure (dimensions, colors, typography, shapes), then build the home screen composables on top. Each step builds on the previous one, and testing tasks are interleaved close to the code they validate.

## Tasks

- [x] 1. Set up theme package and dimensions
  - [x] 1.1 Create `BeepMeDimensions` data class and `LocalBeepMeDimensions` CompositionLocal
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/Dimensions.kt`
    - Define `BeepMeDimensions` with tokens: xxs (2dp), xs (4dp), s (8dp), m (12dp), l (16dp), xl (24dp), xxl (32dp), xxxl (40dp), iconSmall (36dp), iconMedium (40dp), borderWidth (1dp)
    - Define `LocalBeepMeDimensions = staticCompositionLocalOf { BeepMeDimensions() }`
    - _Requirements: 4.1, 4.2_

  - [x] 1.2 Create `BeepMeShapes` using dimension tokens
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/Shape.kt`
    - Define `BeepMeShapes` with small = s (8dp) and medium = m (12dp) rounded corners
    - _Requirements: 4.1_

- [x] 2. Set up typography with Work Sans font
  - [x] 2.1 Add Work Sans font files to compose resources and create `BeepMeTypography`
    - Add Work Sans font files (Regular, Medium, SemiBold, Bold) to `composeApp/src/commonMain/composeResources/font/`
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/Typography.kt`
    - Define `BeepMeTypography` mapping: titleLarge (Bold 24sp), titleSmall (SemiBold 14sp), labelMedium (Medium 14sp), labelSmall (Medium 10sp), bodySmall (Regular 10sp), bodyMedium (Regular 14sp)
    - _Requirements: 3.1, 3.2_

- [x] 3. Implement ColorTheme and BeepMe Classic theme
  - [x] 3.1 Create `ColorTheme` data class and `BeepMeClassicTheme` definition
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/ColorTheme.kt`
    - Define `ColorTheme` data class with name, lightColorScheme, darkColorScheme
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/BeepMeClassicTheme.kt`
    - Define the BeepMe Classic light scheme: background #FFFAF0, surface White, primary #023047, onPrimary White, secondary #780000, onSecondary White, onBackground #023047, onSurface #023047
    - Define the BeepMe Classic dark scheme: background #1A1A1A, surface #2C2C2C, primary #8ECAE6, onPrimary #023047, secondary #FFB3B3, onSecondary #780000, onBackground #FFFAF0, onSurface #E0E0E0
    - _Requirements: 1.1, 1.2, 1.3_

  - [x] 3.2 Write property test for ColorTheme structural invariant
    - **Property 1: ColorTheme structural invariant**
    - **Validates: Requirements 1.1**

  - [x] 3.3 Write unit tests for BeepMe Classic theme colors
    - Assert exact hex values for light variant (background, surface, primary, secondary)
    - Assert dark variant has adjusted colors
    - _Requirements: 1.2, 1.3_

- [x] 4. Implement ThemeManager and BeepMeTheme composable
  - [x] 4.1 Create `ThemeManager` class
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/ThemeManager.kt`
    - Implement with availableThemes list, selectedTheme as StateFlow, and selectTheme() method
    - Ignore selectTheme calls for themes not in the available list
    - _Requirements: 2.1, 2.2_

  - [x] 4.2 Create `BeepMeTheme` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/theme/Theme.kt`
    - Resolve light/dark variant based on system dark mode setting
    - Provide BeepMeDimensions via CompositionLocalProvider
    - Apply colorScheme, BeepMeTypography, and BeepMeShapes through MaterialTheme
    - _Requirements: 2.3, 2.4_

  - [x] 4.3 Write property test for theme selection updates state
    - **Property 2: Theme selection updates state**
    - **Validates: Requirements 2.1, 2.2**

  - [x] 4.4 Write property test for dark mode variant resolution
    - **Property 3: Dark mode variant resolution**
    - **Validates: Requirements 2.3**

  - [x] 4.5 Write unit tests for ThemeManager edge cases
    - Test initialization sets selectedTheme to initial theme
    - Test selecting a theme not in the available list is ignored
    - _Requirements: 2.1, 2.2_

- [x] 5. Checkpoint - Ensure all theme tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement home screen UI models
  - [ ] 6.1 Create UI state models for the home screen
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/home/HomeScreenUiState.kt`
    - Define HomeScreenUiState, BreadcrumbItem, FolderUiModel, FileUiModel data classes
    - _Requirements: 5.1, 6.1, 7.1, 8.1_

- [ ] 7. Implement home screen header and breadcrumb components
  - [ ] 7.1 Create `Header` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/home/Header.kt`
    - Display navigation icon and title using titleLarge typography in primary color
    - _Requirements: 5.1, 5.2_

  - [ ] 7.2 Create `BreadcrumbNav` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/home/BreadcrumbNav.kt`
    - Render path segments in horizontal row using labelMedium typography
    - Last segment in pill-shaped chip (primary background, onPrimary text)
    - Non-current segments as clickable plain text in primary color
    - Invoke navigation callback on tap of non-current segment
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 8. Implement folder and file item components
  - [ ] 8.1 Create `FolderCard` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/home/FolderCard.kt`
    - White background, medium shape corners, borderWidth primary-colored border
    - iconMedium × iconMedium icon container with primary background and small shape corners
    - Folder name in Bold 14sp, item count in Regular 10sp
    - Clickable with onClick callback
    - _Requirements: 7.3, 7.4, 7.5_

  - [ ] 8.2 Create `FileItem` composable
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/home/FileItem.kt`
    - White background, medium shape corners, borderWidth secondary-colored border
    - iconSmall × iconSmall icon container with 5% opacity primary background and small shape corners
    - Title in titleSmall, preview in bodySmall, timestamp in labelSmall
    - Clickable with onClick callback
    - _Requirements: 8.3, 8.4, 8.5_

- [ ] 9. Compose the HomeScreen
  - [ ] 9.1 Create `HomeScreen` composable wiring all components together
    - Create file `composeApp/src/commonMain/kotlin/net/onefivefour/notes/ui/home/HomeScreen.kt`
    - Use background color from active ColorScheme
    - Arrange Header, BreadcrumbNav, folders grid (2-column), and files list in a vertically scrollable layout
    - Display "FOLDERS" section label in labelSmall/primary, show FolderCard items in 2-column grid
    - Display "FILES" section label in labelSmall/primary, show FileItem items in vertical list
    - Hide "FOLDERS" section when folders list is empty
    - Hide "FILES" section when files list is empty
    - _Requirements: 7.1, 7.2, 8.1, 8.2, 9.1, 9.2, 9.3, 9.4_

- [ ] 10. Wire theme into App.kt
  - [ ] 10.1 Update `App.kt` to use `BeepMeTheme` with `ThemeManager`
    - Replace the existing `MaterialTheme` wrapper in App.kt with `BeepMeTheme`
    - Initialize ThemeManager with BeepMeClassicTheme
    - Wire HomeScreen with sample/placeholder data for initial verification
    - _Requirements: 2.3, 2.4_

- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using Kotest Property
- Unit tests validate specific examples and edge cases
- Work Sans font files need to be obtained and placed in composeResources/font/ during task 2.1
