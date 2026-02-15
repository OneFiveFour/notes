# EchoList Design System Rules

## Figma Reference

- File: EchoList (key: `n7TqZmCTgWuaCqlzhrwEBf`)
- Home Screen Frame: node-id `4024:3`
- Only this frame is relevant — other screens in the Figma file belong to other apps.

## Technology Stack

- Language: Kotlin
- Framework: Compose Multiplatform (Android, iOS, JVM, JS, WasmJS)
- UI Library: Material 3 (`compose.material3`)
- Build: Gradle with Kotlin DSL, version catalog
- Styling: Compose `MaterialTheme` with custom `ColorScheme`, `Typography`, `Shapes`
- State: Compose `State`, `StateFlow`, ViewModel
- DI: Koin
- Testing: Kotest (framework, assertions, property-based testing)

## Project Structure

```
composeApp/src/commonMain/kotlin/net/onefivefour/notes/
├── ui/
│   ├── theme/          # Theme system (ColorTheme, ThemeManager, EchoListTheme, Typography, Shapes, Dimensions)
│   └── home/           # Home screen composables (HomeScreen, Header, BreadcrumbNav, FolderCard, FileItem)
├── data/               # Data layer (models, repository, sources)
├── di/                 # Koin modules
└── network/            # Network client (ConnectRPC)
```

## Color Tokens

### EchoList Classic — Light
| Role         | Value     |
|--------------|-----------|
| background   | #FFFAF0   |
| surface      | #FFFFFF   |
| primary      | #023047   |
| onPrimary    | #FFFFFF   |
| secondary    | #780000   |
| onSecondary  | #FFFFFF   |
| onBackground | #023047   |
| onSurface    | #023047   |

### EchoList Classic — Dark
| Role         | Value     |
|--------------|-----------|
| background   | #1A1A1A   |
| surface      | #2C2C2C   |
| primary      | #8ECAE6   |
| onPrimary    | #023047   |
| secondary    | #FFB3B3   |
| onSecondary  | #780000   |
| onBackground | #FFFAF0   |
| onSurface    | #E0E0E0   |

## Typography

Font family: Work Sans (bundled in `composeResources/font/`)

| Material3 Style | Weight     | Size  |
|-----------------|------------|-------|
| titleLarge      | Bold 700   | 24sp  |
| titleSmall      | SemiBold 600 | 14sp |
| labelMedium     | Medium 500 | 14sp  |
| labelSmall      | Medium 500 | 10sp  |
| bodySmall       | Regular 400| 10sp  |
| bodyMedium      | Regular 400| 14sp  |

## Spacing & Sizing Tokens (EchoListDimensions)

| Token       | Value |
|-------------|-------|
| xxs         | 2dp   |
| xs          | 4dp   |
| s           | 8dp   |
| m           | 12dp  |
| l           | 16dp  |
| xl          | 24dp  |
| xxl         | 32dp  |
| xxxl        | 40dp  |
| iconSmall   | 36dp  |
| iconMedium  | 40dp  |
| borderWidth | 1dp   |

Accessed via `LocalEchoListDimensions.current` inside composables.

## Shapes

| Shape  | Corner Radius |
|--------|---------------|
| small  | 8dp (s)       |
| medium | 12dp (m)      |

## Component Patterns

- All composables are stateless — they receive UI state and emit callbacks
- Use `MaterialTheme.colorScheme` for all color references
- Use `MaterialTheme.typography` for all text styles
- Use `LocalEchoListDimensions.current` for spacing and sizing
- Use `MaterialTheme.shapes` for corner rounding
- Folder cards use primary-colored borders; file items use secondary-colored borders
- Icon containers in folder cards: primary background, small shape
- Icon containers in file items: 5% opacity primary background, small shape

## Theme Architecture

- `ColorTheme` data class holds name + light/dark `ColorScheme`
- `ThemeManager` holds available themes and selected theme as `StateFlow`
- `EchoListTheme` composable resolves light/dark variant and applies via `MaterialTheme`
- Adding a new theme = creating a new `ColorTheme` instance, no other code changes

## Figma-to-Code Guidelines

- Figma MCP outputs React + Tailwind — always convert to Kotlin Compose with MaterialTheme tokens
- Never install Tailwind or any web dependencies
- Map Tailwind colors to `MaterialTheme.colorScheme` roles
- Map Tailwind spacing to `EchoListDimensions` tokens
- Map Tailwind font classes to `MaterialTheme.typography` styles
- Reuse existing composables instead of duplicating
- Strive for 1:1 visual parity with the Figma design
