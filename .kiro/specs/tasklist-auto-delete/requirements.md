# Requirements Document

## Introduction

The AutoDelete feature allows users to configure each TaskList so that completed tasks are automatically deleted by the backend instead of merely being marked as done. The frontend exposes a toggle at the top of the TaskList editor and passes the `isAutoDelete` flag through create and update operations. The backend owns all deletion and marking logic; the frontend is responsible only for displaying the toggle, persisting the user's choice in the domain model, and sending it with every create/update request.

Much of the plumbing already exists: the proto schema (`is_auto_delete` on `TaskList`, `CreateTaskListRequest`, `UpdateTaskListRequest`), the domain model (`TaskList.isAutoDelete`), the DTOs (`CreateTaskListParams.isAutoDelete`, `UpdateTaskListParams.isAutoDelete`), and the mapper (`TaskListMapper`) are all wired. The remaining work is primarily verifying the UI toggle integration, ensuring correct default values, and confirming end-to-end flag propagation.

## Glossary

- **TaskList**: A named collection of MainTasks, identified by an ID and stored at a file path. Represented by `net.onefivefour.echolist.domain.model.TaskList`.
- **MainTask**: A top-level task inside a TaskList. May contain zero or more SubTasks. Represented by `net.onefivefour.echolist.domain.model.MainTask`.
- **SubTask**: A child task nested under a MainTask. Represented by `net.onefivefour.echolist.domain.model.SubTask`.
- **AutoDelete**: A per-TaskList boolean setting. When enabled, the backend deletes completed tasks instead of marking them as done.
- **EditTaskListScreen**: The Compose screen where users create or edit a TaskList.
- **EditTaskListViewModel**: The ViewModel that manages UI state for the EditTaskListScreen.
- **TaskListMapper**: The object that converts between Wire-generated proto messages and domain models.
- **CreateTaskListParams**: The DTO used to create a new TaskList via the repository.
- **UpdateTaskListParams**: The DTO used to update an existing TaskList via the repository.

## Requirements

### Requirement 1: AutoDelete Toggle in TaskList Editor

**User Story:** As a user, I want to see a toggle at the top of the TaskList editor, so that I can enable or disable AutoDelete mode for each TaskList.

#### Acceptance Criteria

1. WHEN the EditTaskListScreen is displayed, THE EditTaskListScreen SHALL render a labeled toggle control ("Auto Delete") at the top of the task list content area, above the list of MainTasks.
2. WHEN the user taps the AutoDelete toggle, THE EditTaskListScreen SHALL invoke the `onToggleAutoDelete` callback with the new boolean value.
3. WHILE the TaskList is loading or saving, THE EditTaskListScreen SHALL disable the AutoDelete toggle so the user cannot change the value.
4. THE EditTaskListScreen SHALL reflect the current `isAutoDelete` value from `EditTaskListUiState` as the checked state of the toggle.

### Requirement 2: AutoDelete Default Value on TaskList Creation

**User Story:** As a user, I want new TaskLists to default to AutoDelete off, so that tasks are not unexpectedly deleted.

#### Acceptance Criteria

1. WHEN a new TaskList is created, THE EditTaskListViewModel SHALL initialize the `isAutoDelete` state to `false`.
2. THE CreateTaskListParams SHALL default the `isAutoDelete` field to `false`.

### Requirement 3: AutoDelete State Persistence Through Create Operation

**User Story:** As a user, I want my AutoDelete setting to be sent to the backend when I create a TaskList, so that the backend knows whether to auto-delete completed tasks.

#### Acceptance Criteria

1. WHEN the user saves a new TaskList with AutoDelete enabled, THE EditTaskListViewModel SHALL pass `isAutoDelete = true` in the `CreateTaskListParams` to the TaskListRepository.
2. WHEN the user saves a new TaskList with AutoDelete disabled, THE EditTaskListViewModel SHALL pass `isAutoDelete = false` in the `CreateTaskListParams` to the TaskListRepository.
3. THE TaskListMapper SHALL map `CreateTaskListParams.isAutoDelete` to `CreateTaskListRequest.is_auto_delete` without alteration.

### Requirement 4: AutoDelete State Persistence Through Update Operation

**User Story:** As a user, I want my AutoDelete setting to be sent to the backend when I update a TaskList, so that changes to the AutoDelete mode take effect.

#### Acceptance Criteria

1. WHEN the user saves an existing TaskList, THE EditTaskListViewModel SHALL include the current `isAutoDelete` value in the `UpdateTaskListParams`.
2. THE TaskListMapper SHALL map `UpdateTaskListParams.isAutoDelete` to `UpdateTaskListRequest.is_auto_delete` without alteration.

### Requirement 5: AutoDelete State Restoration on Edit

**User Story:** As a user, I want the AutoDelete toggle to reflect the saved setting when I open an existing TaskList for editing, so that I can see and change the current mode.

#### Acceptance Criteria

1. WHEN an existing TaskList is loaded for editing, THE EditTaskListViewModel SHALL set the `isAutoDelete` UI state to the value returned by the backend in the `TaskList` domain model.
2. THE TaskListMapper SHALL map `tasks.v1.TaskList.is_auto_delete` to `TaskList.isAutoDelete` without alteration.

### Requirement 6: AutoDelete Flag Round-Trip Integrity

**User Story:** As a developer, I want the AutoDelete flag to survive a full create-or-update round trip (domain → proto → backend → proto → domain), so that no data is lost in serialization.

#### Acceptance Criteria

1. FOR ALL valid `CreateTaskListParams`, THE TaskListMapper SHALL produce a `CreateTaskListRequest` whose `is_auto_delete` equals the original `isAutoDelete`, and when the response `TaskList` proto is mapped back to the domain, `TaskList.isAutoDelete` SHALL equal the original value (round-trip property).
2. FOR ALL valid `UpdateTaskListParams`, THE TaskListMapper SHALL produce an `UpdateTaskListRequest` whose `is_auto_delete` equals the original `isAutoDelete`, and when the response `TaskList` proto is mapped back to the domain, `TaskList.isAutoDelete` SHALL equal the original value (round-trip property).

### Requirement 7: UI Behavior Difference Based on AutoDelete Mode

**User Story:** As a user, I want the task list editor to visually adapt when AutoDelete is on, so that I understand the mode I am in.

#### Acceptance Criteria

1. WHILE `isAutoDelete` is `true`, THE MainTaskCard SHALL hide the manual delete icon for each MainTask, because deletion is handled automatically by the backend upon completion.
2. WHILE `isAutoDelete` is `false`, THE MainTaskCard SHALL display the manual delete icon for each MainTask.

### Requirement 8: Checking Tasks Regardless of AutoDelete Mode

**User Story:** As a user, I want to be able to check and uncheck tasks in both AutoDelete modes, so that I can mark progress regardless of the deletion setting.

#### Acceptance Criteria

1. WHEN the user checks a MainTask checkbox, THE EditTaskListViewModel SHALL update the `isDone` state of that MainTask to `true`, regardless of the `isAutoDelete` setting.
2. WHEN the user checks a SubTask checkbox, THE EditTaskListViewModel SHALL update the `isDone` state of that SubTask to `true`, regardless of the `isAutoDelete` setting.
3. WHEN the user unchecks a MainTask checkbox, THE EditTaskListViewModel SHALL update the `isDone` state of that MainTask to `false`, regardless of the `isAutoDelete` setting.
4. WHEN the user unchecks a SubTask checkbox, THE EditTaskListViewModel SHALL update the `isDone` state of that SubTask to `false`, regardless of the `isAutoDelete` setting.
