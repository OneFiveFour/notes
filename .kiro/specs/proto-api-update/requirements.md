# Requirements Document

## Introduction

The EchoList backend introduces breaking changes to its proto API. The primary changes are:

1. FolderService is renamed to FileService (proto package `folder.v1` → `file.v1`).
2. The `GetFolder` RPC is removed (redundant).
3. `ListFolders` is replaced by `ListFiles`, which returns `repeated string entries` instead of `repeated Folder folders`. Folder entries are suffixed with `/`.
4. NoteService and TaskListService proto definitions are updated but their client-side logic is preserved for future editing use.
5. The HomeScreen switches from using NotesRepository.listNotes to FileRepository.listFiles as its sole data source for directory listing.

This is a pre-release change — no migration or backwards compatibility is needed. Existing NoteService and TaskListService logic must not be deleted, as it will be needed for editing notes and tasks later.

## Glossary

- **FileService**: The ConnectRPC service (renamed from FolderService) handling folder CRUD and file listing operations. Proto package: `file.v1`.
- **FolderService** (deprecated): The former name of the file/folder backend service, now renamed to FileService.
- **NoteService**: The ConnectRPC service handling note CRUD operations. Proto package: `notes.v1`.
- **TaskListService**: The ConnectRPC service handling task list CRUD operations. Proto package: `tasks.v1`.
- **Folder**: A domain model representing a folder with path and name fields.
- **Note**: A domain model representing a note with filePath, title, content, and updatedAt fields.
- **Entry**: A string path returned by ListFiles. Folder entries end with `/`, file entries do not.
- **FileRepository**: The repository interface (renamed from FolderRepository) that wraps FileService operations.
- **FileRemoteDataSource**: The remote data source interface (renamed from FolderRemoteDataSource) that wraps FileService RPC calls.
- **ConnectRpcClient**: The shared HTTP client that serializes/deserializes protobuf messages over ConnectRPC.
- **HomeViewModel**: The ViewModel powering the HomeScreen, responsible for loading directory content.

## Requirements

### Requirement 1: Rename FolderService Proto to FileService

**User Story:** As a developer, I want the folder.proto file replaced with a file.proto using the `file.v1` package and FileService name, so that the generated code matches the renamed backend service.

#### Acceptance Criteria

1. THE file.proto file SHALL define the proto package as `file.v1`.
2. THE file.proto file SHALL define FileService with four RPCs: CreateFolder, ListFiles, UpdateFolder, DeleteFolder.
3. THE FileService SHALL not include a GetFolder RPC.
4. THE Folder message SHALL contain path and name fields.
5. THE CreateFolderRequest message SHALL contain parent_path and name fields.
6. THE CreateFolderResponse message SHALL contain a single folder field of type Folder.
7. THE ListFilesRequest message SHALL contain a parent_path field.
8. THE ListFilesResponse message SHALL contain a repeated string entries field.
9. THE UpdateFolderRequest message SHALL contain folder_path and new_name fields.
10. THE UpdateFolderResponse message SHALL contain a single folder field of type Folder.
11. THE DeleteFolderRequest message SHALL contain a folder_path field.
12. THE DeleteFolderResponse message SHALL be an empty message.

### Requirement 2: Rename and Update File Remote Data Source

**User Story:** As a developer, I want the FolderRemoteDataSource renamed to FileRemoteDataSource with updated RPC paths using `file.v1.FileService`, so that network calls reach the renamed backend service.

#### Acceptance Criteria

1. THE FileRemoteDataSource interface SHALL expose createFolder, listFiles, updateFolder, and deleteFolder methods.
2. THE FileRemoteDataSource interface SHALL not expose a getFolder method.
3. THE FileRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/file.v1.FileService/CreateFolder" for createFolder.
4. THE FileRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/file.v1.FileService/ListFiles" for listFiles.
5. THE FileRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/file.v1.FileService/UpdateFolder" for updateFolder.
6. THE FileRemoteDataSourceImpl SHALL call ConnectRpcClient with path "/file.v1.FileService/DeleteFolder" for deleteFolder.
7. THE FileRemoteDataSourceImpl SHALL use proto types from the `file.v1` package for request serialization and response deserialization.

### Requirement 3: Update File Mapper for New Response Shapes

**User Story:** As a developer, I want the FolderMapper updated to handle the new FileService response types including ListFilesResponse returning string entries, so that domain model mapping remains correct.

#### Acceptance Criteria

1. THE FileMapper SHALL map a proto Folder message (from `file.v1` package) to a domain Folder preserving path and name.
2. THE FileMapper SHALL map CreateFolderResponse to a single domain Folder.
3. THE FileMapper SHALL map ListFilesResponse to a List of String entries.
4. THE FileMapper SHALL map UpdateFolderResponse to a single domain Folder.
5. THE FileMapper SHALL map CreateFolderParams to a proto CreateFolderRequest using `file.v1` types.
6. THE FileMapper SHALL map UpdateFolderParams to a proto UpdateFolderRequest using `file.v1` types.
7. THE FileMapper SHALL map DeleteFolderParams to a proto DeleteFolderRequest using `file.v1` types.

### Requirement 4: Rename and Update File Repository

**User Story:** As a developer, I want the FolderRepository renamed to FileRepository with the listFolders method replaced by listFiles returning string entries, so that the repository API matches the new FileService contract.

#### Acceptance Criteria

1. THE FileRepository interface SHALL expose createFolder, listFiles, updateFolder, and deleteFolder methods.
2. THE FileRepository interface SHALL not expose a getFolder method.
3. THE FileRepository.listFiles method SHALL accept a parentPath String and return Result<List<String>>.
4. THE FileRepository.createFolder method SHALL accept CreateFolderParams and return Result<Folder>.
5. THE FileRepository.updateFolder method SHALL accept UpdateFolderParams and return Result<Folder>.
6. THE FileRepository.deleteFolder method SHALL accept DeleteFolderParams and return Result<Unit>.
7. IF a network call fails, THEN THE FileRepositoryImpl SHALL return Result.failure with the caught exception.

### Requirement 5: Update HomeViewModel to Use FileRepository for Directory Listing

**User Story:** As a developer, I want the HomeViewModel to fetch directory content from FileRepository.listFiles instead of NotesRepository.listNotes, so that the HomeScreen gets all its listing data from the FileService alone.

#### Acceptance Criteria

1. THE HomeViewModel SHALL depend on FileRepository for loading directory listings.
2. THE HomeViewModel SHALL not depend on NotesRepository for loading directory listings.
3. WHEN the HomeViewModel loads data, THE HomeViewModel SHALL call FileRepository.listFiles to obtain the list of entries.
4. THE HomeViewModel SHALL extract folder entries (paths ending with `/`) from the string entries list to build FolderUiModel items.
5. THE HomeViewModel SHALL extract file entries (paths not ending with `/`) from the string entries list to build FileUiModel items.
6. WHEN building a FileUiModel from a file entry path, THE HomeViewModel SHALL strip the `note_` prefix from note file names and the `tasks_` prefix from task list file names to derive a clean display title.
7. THE FileUiModel SHALL indicate the file type (note or task list) based on whether the original file name starts with `note_` or `tasks_`.
8. WHEN a folder is created successfully, THE HomeViewModel SHALL reload the directory listing via FileRepository.listFiles.
9. THE HomeViewModel SHALL still depend on FolderRepository for createFolder operations through the renamed FileRepository.

### Requirement 6: Update Koin Dependency Injection Modules

**User Story:** As a developer, I want the Koin DI modules updated to wire the renamed FileRemoteDataSource and FileRepository, so that all dependencies resolve correctly at runtime.

#### Acceptance Criteria

1. THE networkModule SHALL provide a FileRemoteDataSource binding to FileRemoteDataSourceImpl.
2. THE dataModule SHALL provide a FileRepository binding to FileRepositoryImpl.
3. THE navigationModule SHALL inject FileRepository (instead of FolderRepository) into HomeViewModel.
4. THE navigationModule SHALL not inject NotesRepository into HomeViewModel for directory listing.
5. THE networkModule SHALL continue to provide NoteRemoteDataSource and TaskListRemoteDataSource bindings unchanged.
6. THE dataModule SHALL continue to provide NotesRepository and TaskListRepository bindings unchanged.

### Requirement 7: Update Note Proto Definitions

**User Story:** As a developer, I want the notes.proto file updated to match the new backend contract, so that the generated code stays in sync.

#### Acceptance Criteria

1. THE notes.proto file SHALL define NoteService with RPCs: CreateNote, ListNotes, GetNote, UpdateNote, DeleteNote.
2. THE Note message SHALL contain file_path, title, content, and updated_at fields.
3. THE CreateNoteRequest message SHALL contain title, content, and path fields.
4. THE CreateNoteResponse message SHALL contain a single note field of type Note.
5. THE GetNoteResponse message SHALL contain a single note field of type Note.
6. THE UpdateNoteRequest message SHALL contain file_path and content fields.
7. THE UpdateNoteResponse message SHALL contain a single note field of type Note.
8. THE ListNotesResponse message SHALL contain repeated notes of type Note and repeated string entries fields.
9. THE DeleteNoteResponse message SHALL be an empty message.

### Requirement 8: Update Task Proto Definitions

**User Story:** As a developer, I want the tasks.proto file updated to match the new backend contract with the TaskListService name, so that the generated code stays in sync.

#### Acceptance Criteria

1. THE tasks.proto file SHALL define TaskListService with RPCs: CreateTaskList, GetTaskList, ListTaskLists, UpdateTaskList, DeleteTaskList.
2. THE SubTask message SHALL contain description and done fields.
3. THE MainTask message SHALL contain description, done, due_date, recurrence, and repeated sub_tasks fields.
4. THE CreateTaskListRequest message SHALL contain name, path, and repeated tasks fields.
5. THE GetTaskListResponse message SHALL contain file_path, name, repeated tasks, and updated_at fields.
6. THE TaskListEntry message SHALL contain file_path, name, and updated_at fields.
7. THE ListTaskListsResponse message SHALL contain repeated task_lists and repeated string entries fields.
8. THE DeleteTaskListResponse message SHALL be an empty message.

### Requirement 9: Preserve Existing Note and Task Service Client Logic

**User Story:** As a developer, I want the existing NoteService and TaskListService client-side logic (remote data sources, repositories, mappers) preserved, so that editing notes and tasks remains possible in the future.

#### Acceptance Criteria

1. THE NoteRemoteDataSource, NoteRemoteDataSourceImpl, NotesRepository, and NotesRepositoryImpl SHALL remain in the codebase.
2. THE TaskListRemoteDataSource, TaskListRemoteDataSourceImpl, TaskListRepository, and TaskListRepositoryImpl SHALL remain in the codebase.
3. THE NoteMapper and TaskListMapper SHALL remain in the codebase.
4. THE Koin bindings for NoteRemoteDataSource, NotesRepository, TaskListRemoteDataSource, and TaskListRepository SHALL remain active.

### Requirement 10: Adapt HomeScreen UI for Entry-Only Data

**User Story:** As a developer, I want the HomeScreen UI adapted to work with string entries from FileService instead of full Note objects, so that the directory listing displays correctly with the new data source.

#### Acceptance Criteria

1. THE HomeScreen SHALL display folder entries as FolderCard composables using the folder name extracted from the entry path.
2. THE HomeScreen SHALL display file entries as FileItem composables using the file name extracted from the entry path.
3. WHEN only string entries are available (no full Note objects), THE FileUiModel SHALL derive its title from the file name in the entry path.
4. THE FolderUiModel SHALL derive its name from the folder path by extracting the last path segment before the trailing `/`.
5. WHEN a folder entry is tapped, THE HomeScreen SHALL navigate to the folder path.
6. WHEN a file entry is tapped, THE HomeScreen SHALL navigate to the file editing screen using the file path.
