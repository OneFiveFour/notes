# Implementation Plan: Expanding FAB Home Screen

## Overview

This implementation adds an expanding FAB interaction to the home screen with smooth animations. The approach follows EchoList's stateless composable pattern, creating two new composables (ExpandingFab and BottomActionBar) and updating HomeScreen to integrate them. All animations use Compose's declarative APIs with 300ms duration and FastOutSlowInEasing for a cohesive motion feel.

## Tasks

- [x] 1. Create ExpandingFab composable with state management
  - [x] 1.1 Create ExpandingFab.kt file in ui/home package
    - Define ExpandingFab composable with parameters: isExpanded, onToggle, onNoteClick, onTaskClick, onFolderClick, modifier
    - Implement Box layout with Alignment.BottomCenter
    - Add FAB button using RoundIconButton with plus icon
    - _Requirements: 1.1, 1.2, 1.3, 1.4_
  
  - [ ]* 1.2 Write property test for FAB toggle behavior
    - **Property 1: For any FAB expansion state (expanded or collapsed), clicking the FAB button should toggle the state to its opposite value**
    - **Validates: Requirements 1.2, 1.3**
  
  - [ ]* 1.3 Write property test for state stability
    - **Property 2: For any FAB expansion state, if no toggle callback is invoked, the state should remain unchanged over time**
    - **Validates: Requirements 1.4**

- [x] 2. Implement FAB icon rotation animation
  - [x] 2.1 Add rotation animation to ExpandingFab
    - Use animateFloatAsState targeting 0° (collapsed) or 45° (expanded)
    - Configure animation with 300ms duration and FastOutSlowInEasing
    - Apply rotation using Modifier.graphicsLayer { rotationZ = rotation }
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [ ]* 2.2 Write unit tests for rotation animation configuration
    - Test rotation targets 0° when collapsed
    - Test rotation targets 45° when expanded
    - Test animation uses 300ms duration
    - Test animation uses FastOutSlowInEasing
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [ ]* 2.3 Write property test for FAB clickability during animation
    - **Property 3: For any animation state (in-progress or complete), the FAB button should remain clickable and responsive to user input**
    - **Validates: Requirements 2.4**

- [x] 3. Implement CreateItemPill expansion and collapse animations
  - [x] 3.1 Add three AnimatedVisibility blocks for pills
    - Create AnimatedVisibility for Note pill (top position)
    - Create AnimatedVisibility for Task pill (middle position)
    - Create AnimatedVisibility for Folder pill (bottom position)
    - Configure enter animations: fadeIn + scaleIn (initialScale 0.8f) + slideInVertically
    - Configure exit animations: fadeOut + scaleOut (targetScale 0.8f) + slideOutVertically
    - All animations use 300ms duration and FastOutSlowInEasing
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 4.1, 4.2, 4.3, 4.4, 4.5, 7.1, 7.2, 7.3, 7.4_
  
  - [x] 3.2 Configure pill positioning and spacing
    - Position pills using Modifier.offset with 12dp spacing (EchoListTheme.dimensions.m)
    - Lowest pill positioned 12dp above FAB button
    - Ensure horizontal centering relative to FAB
    - _Requirements: 8.1, 8.2, 8.3, 5.5_
  
  - [ ]* 3.3 Write unit tests for animation configuration
    - Test enter animations include fadeIn, scaleIn, slideInVertically
    - Test exit animations include fadeOut, scaleOut, slideOutVertically
    - Test all animations use 300ms duration
    - Test all animations use FastOutSlowInEasing
    - _Requirements: 3.3, 3.6, 4.2, 4.5_
  
  - [ ]* 3.4 Write unit tests for pill spacing
    - Test pills are spaced 12dp apart (m token)
    - Test lowest pill is 12dp above FAB (m token)
    - _Requirements: 5.5, 8.3_

- [x] 4. Configure CreateItemPill components with theme colors
  - [x] 4.1 Add CreateItemPill instances with correct configuration
    - Note pill: text "Note", color EchoListTheme.echoListColorScheme.noteColor
    - Task pill: text "Task", color EchoListTheme.echoListColorScheme.taskColor
    - Folder pill: text "Folder", color EchoListTheme.echoListColorScheme.folderColor
    - Wire onClick callbacks to invoke corresponding callbacks and collapse FAB
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 6.2, 6.3_
  
  - [ ]* 4.2 Write property test for pill configuration correctness
    - **Property 4: For any pill type (Note, Task, or Folder), the rendered pill should display the correct text label and use the corresponding theme color token**
    - **Validates: Requirements 5.1, 5.2, 5.3**
  
  - [ ]* 4.3 Write unit tests for pill configuration
    - Test Note pill has correct text "Note" and noteColor
    - Test Task pill has correct text "Task" and taskColor
    - Test Folder pill has correct text "Folder" and folderColor
    - _Requirements: 5.1, 5.2, 5.3_

- [x] 5. Implement pill click interactions
  - [x] 5.1 Add click handlers to CreateItemPill components
    - Each pill click invokes its corresponding callback (onNoteClick, onTaskClick, onFolderClick)
    - Each pill click triggers FAB collapse by invoking onToggle
    - Pills only clickable when isExpanded is true
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [ ]* 5.2 Write property test for pill click behavior
    - **Property 5: For any pill (Note, Task, or Folder) in expanded state, clicking the pill should invoke its corresponding callback and transition the FAB to collapsed state**
    - **Validates: Requirements 6.2, 6.3**
  
  - [ ]* 5.3 Write property test for pills clickable when expanded
    - **Property 6: For any pill in expanded state, the pill should be clickable and invoke its callback when clicked**
    - **Validates: Requirements 6.1**
  
  - [ ]* 5.4 Write unit tests for pill interactions
    - Test clicking Note pill invokes onNoteClick callback
    - Test clicking Task pill invokes onTaskClick callback
    - Test clicking Folder pill invokes onFolderClick callback
    - Test clicking any pill collapses the FAB
    - _Requirements: 6.2, 6.3_

- [x] 6. Checkpoint - Ensure ExpandingFab composable is complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Create BottomActionBar composable
  - [x] 7.1 Create BottomActionBar composable in HomeScreen.kt
    - Define private BottomActionBar composable with parameters: isFabExpanded, onFabToggle, onSearchClick, onSettingsClick, onNoteClick, onTaskClick, onFolderClick, modifier
    - Implement Row layout with Arrangement.Center and 12dp spacing
    - Add RoundIconButton for Search
    - Add RoundIconButton for Settings
    - Add ExpandingFab component
    - _Requirements: 8.1, 8.2_
  
  - [ ]* 7.2 Write unit tests for BottomActionBar layout
    - Test buttons are horizontally centered
    - Test buttons have 12dp spacing
    - Test correct button order: Search, Settings, ExpandingFab
    - _Requirements: 8.1, 8.2_

- [x] 8. Update HomeScreen to integrate ExpandingFab
  - [x] 8.1 Add FAB expansion state to HomeScreen
    - Add mutableStateOf(false) for isFabExpanded
    - Add callback parameters: onNoteCreate, onTaskCreate, onFolderCreate
    - _Requirements: 1.1, 1.2, 1.3, 1.4_
  
  - [x] 8.2 Replace bottom action row with BottomActionBar
    - Remove existing bottom action row implementation
    - Add BottomActionBar composable call
    - Pass isFabExpanded state and all callbacks
    - _Requirements: 8.1, 8.2, 8.4_
  
  - [ ]* 8.3 Write unit tests for HomeScreen integration
    - Test initial state is collapsed
    - Test toggle from collapsed to expanded
    - Test toggle from expanded to collapsed
    - Test state persists between recompositions
    - _Requirements: 1.1, 1.2, 1.3, 1.4_
  
  - [ ]* 8.4 Write unit tests for visibility
    - Test when collapsed, no pills are visible
    - Test when expanded, exactly three pills are visible
    - Test pills appear in order: Folder (bottom), Task (middle), Note (top)
    - _Requirements: 3.1, 3.2, 4.1_

- [x] 9. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using Kotest
- Unit tests validate specific examples and edge cases
- All animations use EchoListTheme tokens for consistency
- Reuse existing CreateItemPill component to minimize code duplication
