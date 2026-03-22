# EchoList Repository Notes

This repository contains a Kotlin Multiplatform app built with Compose Multiplatform. The `.kiro` folder has a large amount of historical product, architecture, and implementation context from a previous AI-assisted workflow. Use it as process memory, but not as a guaranteed description of the current code. Several later specs supersede earlier ones, and some spec-complete items do not match the current implementation anymore.

## Project Snapshot

- Product: cross-platform productivity app with hierarchical folders, notes, and task lists
- Targets: Android, iOS, JVM desktop, JS web, WasmJS
- Core user flow: authenticate against a self-hosted backend, browse folder contents, create and edit folders/notes/task lists
- Main module: `composeApp/`
- Native iOS wrapper: `iosApp/`
- Protobuf schemas: `proto/`
- Historical/spec context: `.kiro/steering/` and `.kiro/specs/`

## Canonical Architecture Rules

Treat `.kiro/steering/*.md` as the best source for long-lived rules:

- `ui -> domain/repository contracts -> data/repository implementations -> remote/cache data sources`
- Networking uses Ktor plus a custom ConnectRPC client over protobuf
- Wire-generated message classes are build output; never hand-edit generated code
- SQLDelight is the cache layer, with platform-specific drivers provided through Koin
- JWT auth drives top-level app state; tokens live in platform-specific `SecureStorage`
- Repositories should return `Result<T>` instead of throwing across layer boundaries
- In common code prefer `Dispatchers.Default`; do not rely on `Dispatchers.IO`

## UI and Design System Rules

From `.kiro/steering/design-system.md`:

- Use `EchoListTheme.*` accessors, not raw `MaterialTheme.*` or ad hoc tokens
- Use bundled Work Sans fonts from compose resources
- Use `EchoListDimensions` tokens for spacing and sizing
- Use theme shapes instead of raw corner values
- Folder cards use primary borders and primary-filled icon containers
- File items use secondary borders and low-opacity primary icon backgrounds
- Figma MCP output must be translated into Kotlin Compose with project theme tokens; do not pull in Tailwind or web-only styling dependencies

Important cleanup note from `quality-review-cleanup`:

- Standardize on `EchoListTheme.*` everywhere

## Build, Run, and Quality Commands

Useful commands repeatedly referenced by the repo and Kiro notes:

- `./gradlew :composeApp:assembleDebug`
- `./gradlew :composeApp:run`
- `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- `./gradlew test`
- `./gradlew :composeApp:jvmTest`
- `./gradlew detekt`
- `./gradlew ktlintCheck`

On Windows, use `.\gradlew.bat ...`.

## Testing Conventions

Kiro specs consistently treat testing as a first-class part of the workflow:

- Kotest is the main test framework
- Property-based tests use `kotest-property`, usually with 100+ iterations
- `commonTest` is used for pure logic, mapper tests, and shared property tests
- `jvmTest` is used for JUnit 5 runner integration, Ktor plugin tests, and DI-heavy tests
- Common test targets include route serialization, navigation stack behavior, mapper correctness, repository error handling, cache/offline behavior, and UI state-machine invariants

## Kiro History and Major Workstreams

These specs describe the previous implementation process and are useful context:

- `beepme-theme-and-home-screen`: introduced the theme system, typography, home UI models, and main home screen structure
- `compose-navigation-3`: introduced Navigation 3 and serializable routes, but uses older route/screen concepts that were later replaced
- `jwt-authentication`: auth protobufs, secure storage, auth repository, auth interceptor, login flow, dynamic backend config, auth-state-driven top-level UI
- `notes-networking-data-layer`: foundational networking/cache/repository architecture, offline queueing, cache-first reads, and test doubles
- `proto-api-update`: updates file/note/task-list mappers, repositories, and network sources to newer backend protos
- `network-request-logging`: Ktor logging plugin with header redaction and hex formatting for protobuf payloads
- `string-resource-extraction`: moves UI strings into resources
- `expanding-fab-home-screen`: animated bottom action UI and create pills on the home screen
- `create-folder-dialog`: modal folder creation plus `FileRepository.directoryChanged` refresh signaling
- `add-new-item-inline`: later folder-creation UX that replaces the add button cell with an inline editor
- `file-add-button`: older create-note/create-task-list affordance and route wiring
- `note-tasklist-editors`: editor view models, save logic, screen state, and editor navigation plumbing
- `unified-edit-screens`: later refactor intended to replace separate create/detail routes with unified edit routes

## Verified Current Code State

These points were checked directly in the repository and are more trustworthy than the spec history:

- `Routes.kt` currently defines `LoginRoute` and `HomeRoute(path)`, but `EditNoteRoute` and `EditTaskListRoute` are still parameterless `data object`s
- `App.kt` injects `EditNoteViewModel` and `EditTaskListViewModel` with the current `HomeRoute.path` through Koin parameters instead of route parameters
- `EditNoteViewModel` and `EditTaskListViewModel` currently create new items using `parentPath`; they are not loading existing note/task-list content by ID
- `HomeScreen` still renders `CreateFolderDialog`
- `CreateFolderDialog` and `CreateFolderViewModel` are actively wired in `App.kt`
- `CreateItemPills` still uses simple `() -> Unit` callbacks for note/task-list/folder creation
- `FileRepository.directoryChanged` exists and `HomeViewModel` observes it
- `NetworkLoggingPlugin`, `AuthInterceptor`, `NetworkException`, and `NotesRepositoryImpl.pendingOperations` all exist in code
- String resources are used in many screens, but there are still hardcoded strings in at least `CreateFolderDialog`

## Important Spec vs Code Mismatches

Preserve these mismatches in mind before changing navigation or creation flows:

- `unified-edit-screens` expects `EditNoteRoute(noteId: String? = null)` and `EditTaskListRoute(taskListId: String? = null)`, but current code still uses parameterless route objects
- `note-tasklist-editors` expects editor flows to receive `parentPath` through route parameters, but current code derives the path from the active `HomeRoute`
- `add-new-item-inline` describes inline folder creation with `AddItemButton`, `InlineItemEditor`, and `InlineCreationState`, but current code still uses the older folder dialog flow
- `inline-item-creation` planned callback signature changes for typed inline creation, but current code still shows the pre-change callback shape in `CreateItemPills` and `BottomNavigation`
- `string-resource-extraction` aimed for no hardcoded UI strings, but the current folder dialog still contains literal strings

When a spec and the code disagree, trust the code first and treat the spec as a roadmap or historical intent.

## Still Open in Kiro Task Lists

The most relevant unfinished items from the Kiro backlog:

- `inline-item-creation`
  - propagate callback signature changes through `BottomNavigation` and `HomeScreen`
  - final checkpoint/tests
- `quality-review-cleanup`
  - add domain-specific exceptions for force-unwrapped proto fields
  - delete `SaveButtonNoOpPropertyTest`
  - make all Compose previews private
  - migrate more Koin definitions to constructor DSL
  - add `.kiro/steering/koin-conventions.md`
  - standardize repository error handling around `NetworkException`
- `notes-networking-data-layer`
  - task list shows the original ConnectRPC client/retry item as unchecked, even though later code/specs clearly depend on a client implementation

## Practical Guidance For Future Work

- Use `.kiro/steering/echolist-overview.md` for architecture decisions
- Use `.kiro/steering/design-system.md` for all UI styling decisions
- Treat older route specs (`compose-navigation-3`, `file-add-button`) as historical unless you are intentionally studying the app's evolution
- Before editing navigation, verify whether the goal is:
  - keep the current object-route plus injected-parent-path approach
  - move toward the later Kiro unified-edit-route design
- Before editing folder creation UX, verify whether we want to keep the current dialog flow or revive the later inline-editor design
- For networking and repository work, preserve `Result<T>` boundaries, redaction in logs, and offline/cache-first patterns
- For UI work, prefer extending existing shared `commonMain` composables and theme tokens instead of introducing platform-specific variants
