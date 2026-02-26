# Requirements Document

## Introduction

The EchoList backend has undergone a proto API revision that introduces four major changes:

1. The "domain" concept (separate `/data/notes` and `/data/tasks` roots) is removed. All content lives under a single data root folder.
2. API request/response names are unified across FolderService, NoteService, and TaskListService (e.g. `RenameFolder` → `UpdateFolder`).
3. Deletion RPCs now return empty responses instead of a full folder list.
4. The existing TaskService is renamed to TaskListService, with updated CRUD operations and message shapes.

This feature updates the Kotlin data layer (proto files, domain models, mappers, remote data sources, repositories, DI wiring, and tests) to align with the new proto definitions. Since the client does not yet have a task implementation, the TaskListService client code is created fresh to match the renamed backend service. No migration code is needed — this is a pre-release breaking change.

## Glossary

- **FolderService**: The ConnectRPC service handling folder CRUD operations (create, get, list, update, delete).
- **NoteService**: The ConnectRPC service handling note CRUD operations (create, get, list, update, delete).
- **TaskListService**: The ConnectRPC service handling task list CRUD operations (create, get, list, update, delete). Renamed from the former TaskService on the backend.
- **TaskService** (deprecated): The former name of the backend service for task operations, now renamed to TaskListService.
- **Folder**: A domain model representing a folder with a path and name.
- **Note**: A domain model representing a note with file_path, title, content, and updated_at.
- **TaskList**: A domain model representing a task list with file_path, name, tasks, and updated_at.
- **MainTask**: A domain model representing a task with description, done status, due_date, recurrence, and SubTasks.
- **SubTask**: A domain model representing a SubTask with description and done status.
- **Mapper**: An object that converts between Wire-generated proto types and Kotlin domain models.
- **RemoteDataSource**: An interface that wraps ConnectRPC calls for a specific service.
- **Repository**: An interface that orchestrates data access across network and cache sources.
- **ConnectRpcClient**: The shared HTTP client that serializes/deserializes protobuf messages over ConnectRPC.
- **Entry**: An immediate child path returned by list operations; folder paths end with "/", file paths do not.
- **ListResult**: A domain model combining full objects with immediate-child entry paths from a list response.

## Requirements

### Requirement 1: Remove Domain Parameter from Folder Operations

**User Story:** As a developer, I want the domain parameter removed from all folder-related models and API calls, so that the data layer matches the new single-root backend architecture.

#### Acceptance Criteria

1. THE CreateFolderParams model SHALL contain only parentPath and name fields.
2. THE DeleteFolderParams model SHALL contain only folderPath field.
3. THE RenameFolderParams model SHALL be replaced by an UpdateFolderParams model containing folderPath and newName fields.
4. THE FolderMapper SHALL map CreateFolderParams to CreateFolderRequest without a domain field.
5. THE FolderMapper SHALL map UpdateFolderParams to UpdateFolderRequest with folder_path and new_name fields.
6. THE FolderMapper SHALL map DeleteFolderParams to DeleteFolderRequest with only folder_path field.
7. WHEN the HomeViewModel creates a folder, THE HomeViewModel SHALL construct CreateFolderParams without a domain field.

### Requirement 2: Update Folder Proto and Service Definitions

**User Story:** As a developer, I want the folder.proto file and FolderService RPCs updated, so that the generated code matches the new backend contract.

#### Acceptance Criteria

1. THE folder.proto file SHALL define five RPCs: CreateFolder, GetFolder, ListFolders, UpdateFolder, DeleteFolder.
2. THE Folder message SHALL contain path and name fields.
3. THE CreateFolderRequest message SHALL contain parent_path and name fields without a domain field.
4. THE CreateFolderResponse message SHALL contain a single folder field of type Folder.
5. THE GetFolderRequest message SHALL contain a folder_path field.
6. THE GetFolderResponse message SHALL contain a single folder field of type Folder.
7. THE ListFoldersRequest message SHALL contain a parent_path field.
8. THE ListFoldersResponse message SHALL contain a repeated folders field of type Folder.
9. THE UpdateFolderRequest message SHALL contain folder_path and new_name fields.
10. THE UpdateFolderResponse message SHALL contain a single folder field of type Folder.
11. THE DeleteFolderRequest message SHALL contain a folder_path field.
12. THE DeleteFolderResponse message SHALL be an empty message.

### Requirement 3: Update Folder Domain Model and Mapper

**User Story:** As a developer, I want the Folder domain model to include a name field and the mapper to handle the new response shapes, so that the app can display folder names from the API.

#### Acceptance Criteria

1. THE Folder domain model SHALL contain path and name fields.
2. THE FolderMapper SHALL map a proto Folder message to a domain Folder preserving path and name.
3. THE FolderMapper SHALL map CreateFolderResponse to a single domain Folder.
4. THE FolderMapper SHALL map GetFolderResponse to a single domain Folder.
5. THE FolderMapper SHALL map ListFoldersResponse to a list of domain Folder objects.
6. THE FolderMapper SHALL map UpdateFolderResponse to a single domain Folder.
7. THE FolderMapper SHALL not map DeleteFolderResponse to any Folder (empty response).

### Requirement 4: Update Folder Remote Data Source and Repository

**User Story:** As a developer, I want the FolderRemoteDataSource and FolderRepository updated with the new RPC set, so that the app can perform all folder CRUD operations against the new API.

#### Acceptance Criteria

1. THE FolderRemoteDataSource interface SHALL expose createFolder, getFolder, listFolders, updateFolder, and deleteFolder methods.
2. THE FolderRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/folder.v1.FolderService/CreateFolder" for createFolder.
3. THE FolderRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/folder.v1.FolderService/GetFolder" for getFolder.
4. THE FolderRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/folder.v1.FolderService/ListFolders" for listFolders.
5. THE FolderRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/folder.v1.FolderService/UpdateFolder" for updateFolder.
6. THE FolderRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/folder.v1.FolderService/DeleteFolder" for deleteFolder.
7. THE FolderRepository interface SHALL expose createFolder, getFolder, listFolders, updateFolder, and deleteFolder methods.
8. THE FolderRepository.deleteFolder method SHALL return Result<Unit> instead of Result<List<Folder>>.
9. THE FolderRepository.createFolder method SHALL return Result<Folder> instead of Result<List<Folder>>.
10. THE FolderRepository.updateFolder method SHALL return Result<Folder>.

### Requirement 5: Update Note Proto and Service Definitions

**User Story:** As a developer, I want the notes.proto file updated, so that the generated code matches the new backend contract.

#### Acceptance Criteria

1. THE notes.proto file SHALL define the service as NoteService with RPCs: CreateNote, ListNotes, GetNote, UpdateNote, DeleteNote.
2. THE Note message SHALL contain file_path, title, content, and updated_at fields.
3. THE CreateNoteRequest message SHALL contain title, content, and path fields.
4. THE CreateNoteResponse message SHALL contain a single note field of type Note.
5. THE ListNotesResponse message SHALL contain repeated notes and repeated entries fields.
6. THE GetNoteResponse message SHALL contain a single note field of type Note.
7. THE UpdateNoteRequest message SHALL contain file_path and content fields.
8. THE UpdateNoteResponse message SHALL contain a single note field of type Note.
9. THE DeleteNoteResponse message SHALL be an empty message.

### Requirement 6: Update Note Mapper for New Response Shapes

**User Story:** As a developer, I want the NoteMapper updated to handle the new response message shapes, so that domain model mapping remains correct.

#### Acceptance Criteria

1. THE NoteMapper SHALL map CreateNoteResponse (containing a note field) to a domain Note.
2. THE NoteMapper SHALL map GetNoteResponse (containing a note field) to a domain Note.
3. THE NoteMapper SHALL map UpdateNoteResponse (containing a note field) to a domain Note.
4. THE NoteMapper SHALL map ListNotesResponse to a ListNotesResult containing notes and entries.
5. WHEN the UpdateNoteResponse contains a full Note, THE NotesRepositoryImpl SHALL use the mapped Note directly without a separate GetNote call.

### Requirement 7: Update Note Remote Data Source Service Path

**User Story:** As a developer, I want the NoteRemoteDataSourceImpl to use the renamed service path, so that RPC calls reach the correct backend endpoint.

#### Acceptance Criteria

1. THE NoteRemoteDataSourceImpl SHALL call ConnectRpcClient with paths using "/notes.v1.NoteService/" instead of "/notes.v1.NotesService/".

### Requirement 8: Update TaskList Proto and Service Definitions

**User Story:** As a developer, I want the tasks.proto file updated to reflect the renamed TaskListService and its revised message shapes, so that the generated code matches the new backend contract.

#### Acceptance Criteria

1. THE tasks.proto file SHALL define TaskListService (renamed from TaskService) with RPCs: CreateTaskList, GetTaskList, ListTaskLists, UpdateTaskList, DeleteTaskList.
2. THE SubTask message SHALL contain description and done fields.
3. THE MainTask message SHALL contain description, done, due_date, recurrence, and repeated SubTasks fields.
4. THE CreateTaskListRequest message SHALL contain name, path, and repeated tasks fields.
5. THE CreateTaskListResponse message SHALL contain file_path, name, repeated tasks, and updated_at fields.
6. THE GetTaskListRequest message SHALL contain a file_path field.
7. THE GetTaskListResponse message SHALL contain file_path, name, repeated tasks, and updated_at fields.
8. THE ListTaskListsRequest message SHALL contain a path field.
9. THE TaskListEntry message SHALL contain file_path, name, and updated_at fields.
10. THE ListTaskListsResponse message SHALL contain repeated task_lists and repeated entries fields.
11. THE UpdateTaskListRequest message SHALL contain file_path and repeated tasks fields.
12. THE UpdateTaskListResponse message SHALL contain file_path, name, repeated tasks, and updated_at fields.
13. THE DeleteTaskListRequest message SHALL contain a file_path field.
14. THE DeleteTaskListResponse message SHALL be an empty message.

### Requirement 9: Update TaskList Domain Models

**User Story:** As a developer, I want Kotlin domain models for task lists, MainTasks, and SubTasks updated to match the renamed TaskListService, so that the app has typed representations of the revised task data.

#### Acceptance Criteria

1. THE SubTask domain model SHALL contain description (String) and done (Boolean) fields.
2. THE MainTask domain model SHALL contain description (String), done (Boolean), dueDate (String), recurrence (String), and subTasks (List of SubTask) fields.
3. THE TaskList domain model SHALL contain filePath (String), name (String), tasks (List of MainTask), and updatedAt (Long) fields.
4. THE TaskListEntry domain model SHALL contain filePath (String), name (String), and updatedAt (Long) fields.
5. THE ListTaskListsResult domain model SHALL contain taskLists (List of TaskListEntry) and entries (List of String) fields.
6. THE CreateTaskListParams domain model SHALL contain name (String), path (String), and tasks (List of MainTask) fields.
7. THE UpdateTaskListParams domain model SHALL contain filePath (String) and tasks (List of MainTask) fields.

### Requirement 10: Update TaskList Mapper

**User Story:** As a developer, I want the TaskListMapper updated to convert between the renamed proto messages and domain models, so that the data layer can translate task list API responses.

#### Acceptance Criteria

1. THE TaskListMapper SHALL map a proto SubTask to a domain SubTask preserving description and done.
2. THE TaskListMapper SHALL map a proto MainTask to a domain MainTask preserving all fields including nested SubTasks.
3. THE TaskListMapper SHALL map CreateTaskListResponse to a domain TaskList.
4. THE TaskListMapper SHALL map GetTaskListResponse to a domain TaskList.
5. THE TaskListMapper SHALL map ListTaskListsResponse to a domain ListTaskListsResult.
6. THE TaskListMapper SHALL map UpdateTaskListResponse to a domain TaskList.
7. THE TaskListMapper SHALL map CreateTaskListParams to a proto CreateTaskListRequest including nested MainTask and SubTask conversion.
8. THE TaskListMapper SHALL map UpdateTaskListParams to a proto UpdateTaskListRequest including nested MainTask and SubTask conversion.
9. FOR ALL valid TaskList domain objects, mapping to proto and back to domain SHALL produce an equivalent object (round-trip property).

### Requirement 11: Update TaskList Remote Data Source

**User Story:** As a developer, I want the TaskListRemoteDataSource updated to wrap ConnectRPC calls for the renamed TaskListService, so that the repository can access the task list API.

#### Acceptance Criteria

1. THE TaskListRemoteDataSource interface SHALL expose createTaskList, getTaskList, listTaskLists, updateTaskList, and deleteTaskList methods.
2. THE TaskListRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/tasks.v1.TaskListService/CreateTaskList" for createTaskList.
3. THE TaskListRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/tasks.v1.TaskListService/GetTaskList" for getTaskList.
4. THE TaskListRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/tasks.v1.TaskListService/ListTaskLists" for listTaskLists.
5. THE TaskListRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/tasks.v1.TaskListService/UpdateTaskList" for updateTaskList.
6. THE TaskListRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/tasks.v1.TaskListService/DeleteTaskList" for deleteTaskList.

### Requirement 12: Update TaskList Repository

**User Story:** As a developer, I want the TaskListRepository updated to orchestrate task list data access for the renamed TaskListService, so that the UI layer has a clean API for task list operations.

#### Acceptance Criteria

1. THE TaskListRepository interface SHALL expose createTaskList, getTaskList, listTaskLists, updateTaskList, and deleteTaskList methods.
2. THE TaskListRepository.createTaskList method SHALL accept CreateTaskListParams and return Result<TaskList>.
3. THE TaskListRepository.getTaskList method SHALL accept a filePath String and return Result<TaskList>.
4. THE TaskListRepository.listTaskLists method SHALL accept a path String and return Result<ListTaskListsResult>.
5. THE TaskListRepository.updateTaskList method SHALL accept UpdateTaskListParams and return Result<TaskList>.
6. THE TaskListRepository.deleteTaskList method SHALL accept a filePath String and return Result<Unit>.
7. IF a network call fails, THEN THE TaskListRepositoryImpl SHALL return Result.failure with the caught exception.

### Requirement 13: Update Koin Dependency Injection Modules

**User Story:** As a developer, I want the Koin DI modules updated to wire the new and changed data sources and repositories, so that all dependencies resolve correctly at runtime.

#### Acceptance Criteria

1. THE networkModule SHALL provide a TaskListRemoteDataSource binding to TaskListRemoteDataSourceImpl (updated from former TaskService wiring).
2. THE dataModule SHALL provide a TaskListRepository binding to TaskListRepositoryImpl (updated from former TaskService wiring).
3. THE dataModule SHALL provide FolderRepository bound to the updated FolderRepositoryImpl.
4. THE networkModule SHALL provide FolderRemoteDataSource bound to the updated FolderRemoteDataSourceImpl.
