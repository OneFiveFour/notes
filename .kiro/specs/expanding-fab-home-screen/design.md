# Design Document: Expanding FAB Home Screen

## Overview

This feature enhances the home screen with an expanding FAB (Floating Action Button) interaction that reveals three creation options when activated. The design leverages Compose Multiplatform's animation APIs to create a smooth, coordinated expansion/collapse animation with synchronized icon rotation.

The implementation follows EchoList's stateless composable pattern, where the FAB expansion state is managed by the parent HomeScreen composable and passed down to child components. All animations use Compose's declarative animation APIs with consistent 300ms duration and easing curves.

Key design decisions:
- Use `AnimatedVisibility` for pill appearance/disappearance with scale and slide animations
- Use `animateFloatAsState` for icon rotation with `FastOutSlowInEasing`
- Maintain existing `RoundIconButton` component, adding rotation via `Modifier.graphicsLayer`
- Position pills using `Box` with `Alignment.BottomCenter` and offset calculations
- Reuse existing `CreateItemPill` component with theme color tokens

## Architecture

### Component Hierarchy

```
HomeScreen (stateful)
├── BreadcrumbBar
├── Content Area
├── Recent Section
└── BottomActionBar (new)
    ├── RoundIconButton (Search)
    ├── RoundIconButton (Settings)
    └── ExpandingFab (new)
        ├── CreateItemPill (Note) - conditionally visible
        ├── CreateItemPill (Task) - conditionally visible
        ├── CreateItemPill (Folder) - conditionally visible
        └── RoundIconButton (Plus/Close)
```

### State Management

The expansion state is managed at the `HomeScreen` level using a simple boolean state variable:

```kotlin
var isFabExpanded by remember { mutableStateOf(false) }
```

This state is passed to the `ExpandingFab` composable, which handles:
- Rendering the FAB button with rotation animation
- Conditionally rendering the three `CreateItemPill` components
- Coordinating all animations based on the expansion state

### Animation Strategy

All animations use a consistent 300ms duration with `FastOutSlowInEasing` to create a cohesive motion feel:

1. **Icon Rotation**: `animateFloatAsState` targeting 0° (collapsed) or 45° (expanded)
2. **Pill Visibility**: `AnimatedVisibility` with `fadeIn + scaleIn` and `fadeOut + scaleOut`
3. **Pill Translation**: Each pill uses `slideInVertically` and `slideOutVertically` with different offsets

The animations are synchronized by triggering on the same state change (`isFabExpanded`), ensuring the rotation and expansion/collapse happen simultaneously.

## Components and Interfaces

### ExpandingFab Composable

A new stateless composable that encapsulates the expanding FAB behavior.

**Signature:**
```kotlin
@Composable
fun ExpandingFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNoteClick: () -> Unit,
    onTaskClick: () -> Unit,
    onFolderClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Parameters:**
- `isExpanded`: Boolean indicating current expansion state
- `onToggle`: Callback invoked when FAB button is clicked
- `onNoteClick`: Callback invoked when Note pill is clicked
- `onTaskClick`: Callback invoked when Task pill is clicked
- `onFolderClick`: Callback invoked when Folder pill is clicked
- `modifier`: Optional modifier for positioning

**Responsibilities:**
- Render the FAB button with rotation animation
- Render three `CreateItemPill` components with visibility animations
- Handle click events and invoke appropriate callbacks
- Manage layout positioning with proper spacing

**Layout Structure:**
Uses a `Box` with `Alignment.BottomCenter` to stack pills vertically above the FAB button. Each pill is offset using `Modifier.offset` to achieve the 12dp spacing.

### HomeScreen Updates

The existing `HomeScreen` composable will be modified to:
1. Add `isFabExpanded` state variable
2. Replace the bottom action row with a new `BottomActionBar` composable
3. Pass expansion state and callbacks to `ExpandingFab`

**New Callbacks:**
```kotlin
onNoteCreate: () -> Unit
onTaskCreate: () -> Unit
onFolderCreate: () -> Unit
```

These callbacks will be added to `HomeScreen` parameters and passed through to `ExpandingFab`.

### BottomActionBar Composable

A new composable that organizes the bottom action buttons.

**Signature:**
```kotlin
@Composable
private fun BottomActionBar(
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNoteClick: () -> Unit,
    onTaskClick: () -> Unit,
    onFolderClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Layout:**
Uses a `Row` with `Arrangement.Center` to horizontally center the three buttons (Search, Settings, ExpandingFab) with 12dp spacing between them.

### Animation Specifications

**Icon Rotation:**
```kotlin
val rotation by animateFloatAsState(
    targetValue = if (isExpanded) 45f else 0f,
    animationSpec = tween(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
)
```

**Pill Visibility:**
```kotlin
AnimatedVisibility(
    visible = isExpanded,
    enter = fadeIn(
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ),
    exit = fadeOut(
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + slideOutVertically(
        targetOffsetY = { it / 2 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
) {
    CreateItemPill(...)
}
```

## Data Models

### FAB State

The expansion state is represented by a simple boolean value:

```kotlin
data class FabState(
    val isExpanded: Boolean = false
)
```

However, for this implementation, we use a primitive `Boolean` directly in the composable state, as the state is simple and doesn't require additional properties.

### Pill Configuration

Each pill is configured with:
- `text`: String - Display text ("Note", "Task", "Folder")
- `color`: Color - Theme color token
- `onClick`: () -> Unit - Click callback

These are defined inline in the `ExpandingFab` composable using the existing `EchoListTheme.echoListColorScheme` color tokens:
- `noteColor` for Note pill
- `taskColor` for Task pill
- `folderColor` for Folder pill

### Spacing Constants

All spacing uses `EchoListTheme.dimensions` tokens:
- Pill vertical spacing: `m` (12dp)
- Button horizontal spacing: `m` (12dp)
- FAB to first pill spacing: `m` (12dp)


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: FAB Toggle Behavior

*For any* FAB expansion state (expanded or collapsed), clicking the FAB button should toggle the state to its opposite value.

**Validates: Requirements 1.2, 1.3**

### Property 2: State Stability

*For any* FAB expansion state, if no toggle callback is invoked, the state should remain unchanged over time.

**Validates: Requirements 1.4**

### Property 3: FAB Remains Clickable During Animation

*For any* animation state (in-progress or complete), the FAB button should remain clickable and responsive to user input.

**Validates: Requirements 2.4**

### Property 4: Pill Configuration Correctness

*For any* pill type (Note, Task, or Folder), the rendered pill should display the correct text label and use the corresponding theme color token.

**Validates: Requirements 5.1, 5.2, 5.3**

### Property 5: Pill Click Behavior

*For any* pill (Note, Task, or Folder) in expanded state, clicking the pill should invoke its corresponding callback and transition the FAB to collapsed state.

**Validates: Requirements 6.2, 6.3**

### Property 6: Pills Clickable When Expanded

*For any* pill in expanded state, the pill should be clickable and invoke its callback when clicked.

**Validates: Requirements 6.1**

## Error Handling

### Animation Interruption

If a user clicks the FAB button while an animation is in progress, the current animation should be interrupted and a new animation should begin targeting the new state. Compose's `animateFloatAsState` and `AnimatedVisibility` handle this automatically by:
- Canceling the current animation
- Starting a new animation from the current value to the new target
- Maintaining smooth transitions without jarring jumps

### Missing Theme Colors

If theme color tokens (`noteColor`, `taskColor`, `folderColor`) are not properly initialized, the pills will render with `Color.Unspecified`, which appears as black. This is a configuration error that should be caught during theme setup. The design assumes proper theme initialization via `EchoListTheme`.

### Callback Errors

If any of the pill click callbacks (`onNoteClick`, `onTaskClick`, `onFolderClick`) throw exceptions, the error will propagate to the parent composable. The FAB will still collapse as intended since the state change happens before the callback is invoked. Error handling for business logic should be implemented in the callback handlers at the ViewModel or screen level.

### Layout Overflow

If the screen height is insufficient to display all three pills above the FAB without overlapping other content, the pills may overlap with the "Recent" section. This is mitigated by:
- Using reasonable pill heights (standard button height)
- Positioning pills in a compact vertical stack
- Assuming minimum screen height of 480dp (standard Android minimum)

For very small screens, consider using a modal bottom sheet instead of the expanding FAB pattern.

## Testing Strategy

### Unit Testing

Unit tests will verify specific examples and edge cases using Kotest:

**State Management Tests:**
- Initial state is collapsed
- Toggle from collapsed to expanded
- Toggle from expanded to collapsed
- State persists between recompositions

**Component Configuration Tests:**
- Note pill has correct text "Note" and noteColor
- Task pill has correct text "Task" and taskColor
- Folder pill has correct text "Folder" and folderColor
- Pills are spaced 12dp apart (m token)
- Lowest pill is 12dp above FAB (m token)

**Animation Configuration Tests:**
- Rotation animation targets 0° when collapsed, 45° when expanded
- All animations use 300ms duration
- All animations use FastOutSlowInEasing
- Enter animations include fadeIn, scaleIn, slideInVertically
- Exit animations include fadeOut, scaleOut, slideOutVertically

**Interaction Tests:**
- Clicking FAB when collapsed sets isExpanded to true
- Clicking FAB when expanded sets isExpanded to false
- Clicking Note pill invokes onNoteClick callback
- Clicking Task pill invokes onTaskClick callback
- Clicking Folder pill invokes onFolderClick callback
- Clicking any pill collapses the FAB

**Visibility Tests:**
- When collapsed, no pills are visible
- When expanded, exactly three pills are visible
- Pills appear in order: Folder (bottom), Task (middle), Note (top)

### Property-Based Testing

Property tests will verify universal behaviors across all inputs using Kotest's property testing framework with minimum 100 iterations per test:

**Property Test 1: FAB Toggle Behavior**
- Generate random initial states (expanded/collapsed)
- Click FAB and verify state toggles to opposite
- **Tag: Feature: expanding-fab-home-screen, Property 1: For any FAB expansion state (expanded or collapsed), clicking the FAB button should toggle the state to its opposite value**

**Property Test 2: State Stability**
- Generate random initial states
- Wait without invoking toggle callback
- Verify state remains unchanged
- **Tag: Feature: expanding-fab-home-screen, Property 2: For any FAB expansion state, if no toggle callback is invoked, the state should remain unchanged over time**

**Property Test 3: FAB Remains Clickable During Animation**
- Generate random animation states (various progress points)
- Verify FAB click handler is always invoked
- **Tag: Feature: expanding-fab-home-screen, Property 3: For any animation state (in-progress or complete), the FAB button should remain clickable and responsive to user input**

**Property Test 4: Pill Configuration Correctness**
- Generate random pill types (Note, Task, Folder)
- Verify each pill has correct text and color mapping
- **Tag: Feature: expanding-fab-home-screen, Property 4: For any pill type (Note, Task, or Folder), the rendered pill should display the correct text label and use the corresponding theme color token**

**Property Test 5: Pill Click Behavior**
- Generate random pill types
- Click pill and verify callback invoked and FAB collapses
- **Tag: Feature: expanding-fab-home-screen, Property 5: For any pill (Note, Task, or Folder) in expanded state, clicking the pill should invoke its corresponding callback and transition the FAB to collapsed state**

**Property Test 6: Pills Clickable When Expanded**
- Generate random pills in expanded state
- Verify click handlers are invoked
- **Tag: Feature: expanding-fab-home-screen, Property 6: For any pill in expanded state, the pill should be clickable and invoke its callback when clicked**

### Testing Library Configuration

**Framework:** Kotest 5.x
- `kotest-framework-engine` for test structure
- `kotest-assertions-core` for assertions
- `kotest-property` for property-based testing

**Property Test Configuration:**
```kotlin
PropertyTesting.defaultIterationCount = 100
```

Each property test will use Kotest's `forAll` or `checkAll` functions to generate test cases and verify properties hold across all generated inputs.

### Integration Testing

Integration tests will verify the complete interaction flow:
- User clicks FAB → pills appear with animation → user clicks pill → callback invoked → pills disappear
- Multiple rapid FAB clicks → animations interrupt smoothly
- FAB expansion → screen rotation → state preserved

These tests will use Compose UI testing framework with `ComposeUiTest` and semantic matchers.
