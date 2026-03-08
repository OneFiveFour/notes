# Requirements Document

## Introduction

This feature adds an expanding FAB (Floating Action Button) interaction to the home screen. When the user taps the primary action button (plus icon), it expands to reveal three creation options (Note, Task, Folder) as pill-shaped buttons. The interaction includes smooth rotation animation of the trigger button and animated appearance/disappearance of the option pills.

## Glossary

- **FAB_Button**: The primary round icon button with a plus icon at the bottom of the home screen
- **CreateItemPill**: A pill-shaped button component that displays a creation option (Note, Task, or Folder)
- **Expanded_State**: The state when the three CreateItemPill components are visible above the FAB_Button
- **Collapsed_State**: The state when only the FAB_Button is visible with no CreateItemPill components shown
- **Rotation_Animation**: The visual transformation of the FAB_Button icon rotating 45 degrees
- **Expansion_Animation**: The animated appearance of CreateItemPill components from the FAB_Button position
- **Collapse_Animation**: The animated disappearance of CreateItemPill components back to the FAB_Button position

## Requirements

### Requirement 1: FAB Button State Management

**User Story:** As a user, I want to toggle between collapsed and expanded states, so that I can access creation options when needed and dismiss them when not needed.

#### Acceptance Criteria

1. THE FAB_Button SHALL maintain a boolean state indicating whether it is in Expanded_State or Collapsed_State
2. WHEN the FAB_Button is clicked in Collapsed_State, THE FAB_Button SHALL transition to Expanded_State
3. WHEN the FAB_Button is clicked in Expanded_State, THE FAB_Button SHALL transition to Collapsed_State
4. THE FAB_Button SHALL preserve its state until explicitly toggled by user interaction

### Requirement 2: Rotation Animation

**User Story:** As a user, I want visual feedback when toggling the FAB, so that I understand the button's state has changed.

#### Acceptance Criteria

1. WHEN the FAB_Button transitions to Expanded_State, THE FAB_Button icon SHALL rotate 45 degrees clockwise within 300 milliseconds
2. WHEN the FAB_Button transitions to Collapsed_State, THE FAB_Button icon SHALL rotate back to 0 degrees within 300 milliseconds
3. THE Rotation_Animation SHALL use an easing curve for smooth visual transition
4. WHILE the Rotation_Animation is in progress, THE FAB_Button SHALL remain clickable

### Requirement 3: CreateItemPill Expansion

**User Story:** As a user, I want to see creation options appear smoothly, so that the interface feels responsive and polished.

#### Acceptance Criteria

1. WHEN the FAB_Button transitions to Expanded_State, THE system SHALL display three CreateItemPill components above the FAB_Button
2. THE CreateItemPill components SHALL appear in vertical stack order from bottom to top: Folder, Task, Note
3. THE Expansion_Animation SHALL complete within 300 milliseconds
4. WHILE expanding, THE CreateItemPill components SHALL animate from the FAB_Button position to their final positions
5. THE CreateItemPill components SHALL use scale and translation animations during expansion
6. THE Expansion_Animation SHALL use an easing curve for smooth visual transition

### Requirement 4: CreateItemPill Collapse

**User Story:** As a user, I want creation options to disappear smoothly when dismissed, so that the interface returns to its original state cleanly.

#### Acceptance Criteria

1. WHEN the FAB_Button transitions to Collapsed_State, THE system SHALL hide all CreateItemPill components
2. THE Collapse_Animation SHALL complete within 300 milliseconds
3. WHILE collapsing, THE CreateItemPill components SHALL animate from their current positions back to the FAB_Button position
4. THE CreateItemPill components SHALL use scale and translation animations during collapse
5. THE Collapse_Animation SHALL use an easing curve for smooth visual transition

### Requirement 5: CreateItemPill Content and Styling

**User Story:** As a user, I want to distinguish between different creation options, so that I can quickly identify which type of item I want to create.

#### Acceptance Criteria

1. THE first CreateItemPill SHALL display the text "Note" and use the note color from the theme
2. THE second CreateItemPill SHALL display the text "Task" and use the task color from the theme
3. THE third CreateItemPill SHALL display the text "Folder" and use the folder color from the theme
4. THE CreateItemPill components SHALL use the existing CreateItemPill composable implementation
5. THE CreateItemPill components SHALL maintain consistent spacing of 12dp (m token) between each pill

### Requirement 6: CreateItemPill Interaction

**User Story:** As a user, I want to click on creation options, so that I can initiate the creation of different item types.

#### Acceptance Criteria

1. WHILE in Expanded_State, THE CreateItemPill components SHALL be clickable
2. WHEN a CreateItemPill is clicked, THE system SHALL invoke the corresponding onClick callback
3. WHEN a CreateItemPill is clicked, THE system SHALL transition to Collapsed_State
4. WHILE in Collapsed_State, THE CreateItemPill components SHALL not be clickable

### Requirement 7: Animation Synchronization

**User Story:** As a user, I want all animations to feel coordinated, so that the interaction appears as a single cohesive motion.

#### Acceptance Criteria

1. WHEN transitioning to Expanded_State, THE Rotation_Animation and Expansion_Animation SHALL start simultaneously
2. WHEN transitioning to Collapsed_State, THE Rotation_Animation and Collapse_Animation SHALL start simultaneously
3. THE Rotation_Animation and Expansion_Animation SHALL complete within the same duration
4. THE Rotation_Animation and Collapse_Animation SHALL complete within the same duration

### Requirement 8: Layout Positioning

**User Story:** As a user, I want the creation options to appear in a logical position, so that I can easily reach them without obscuring other content.

#### Acceptance Criteria

1. THE CreateItemPill components SHALL be positioned directly above the FAB_Button
2. THE CreateItemPill components SHALL be horizontally centered relative to the FAB_Button
3. THE lowest CreateItemPill SHALL be positioned 12dp (m token) above the FAB_Button
4. THE CreateItemPill components SHALL not overlap with other UI elements in the home screen
