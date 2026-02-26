# Implementation Tasks

## Task 1: Update Folder Proto Definition
- [x] 1.1 Replace `proto/folder.proto` with the new proto definition: remove domain fields, rename FolderEntry to Folder (with path + name), add GetFolder and ListFolders RPCs, rename RenameFolder to UpdateFolder, make DeleteFolderResponse empty, update all request/response messages per Requirement 2
- [x] 1.2 Run Wire code generation to produce updated proto classes (`./gradlew generateProtos` or equivalent)

## Task 2: Update Folder Domain Models
- [x] 2.1 Update `Folder.kt` to add `name: String` field (Req 3.1)
- [x] 2.2 Update `CreateFolderParams.kt` to remove `domain` field, keeping only `parentPath` and `name` (Req 1.1)
- [x] 2.3 Update `DeleteFolderParams.kt` to remove `domain` field, keeping only `folderPath` (Req 1.2)
- [x] 2.4 Replace `RenameFolderParams.kt` with `UpdateFolderParams.kt` containing `folderPath` and `newName` (Req 1.3)

## Task 3: Update FolderMapper
- [x] 3.1 Rewrite `FolderMapper` to map proto `Folder` → domain `Folder` (path + name) (Req 3.2)
- [x] 3.2 Add mapping for `CreateFolderResponse` → single domain `Folder` (Req 3.3)
- [x] 3.3 Add mapping for `GetFolderResponse` → single domain `Folder` (Req 3.4)
- [x] 3.4 Add mapping for `ListFoldersResponse` → `List<Folder>` (Req 3.5)
- [x] 3.5 Add mapping for `UpdateFolderResponse` → single domain `Folder` (Req 3.6)
- [x] 3.6 Update domain→proto mappings: `CreateFolderParams` → `CreateFolderRequest` without domain (Req 1.4), `UpdateFolderParams` → `UpdateFolderRequest` (Req 1.5), `DeleteFolderParams` → `DeleteFolderRequest` without domain (Req 1.6)
- [x] 3.7 Remove old `RenameFolderRequest`/`RenameFolderResponse`/`DeleteFolderResponse` list mappings

## Task 4: Update FolderRemoteDataSource
- [x] 4.1 Update `FolderRemoteDataSource` interface: replace `renameFolder` with `updateFolder`, add `getFolder` and `listFolders` methods (Req 4.1)
- [x] 4.2 Update `FolderRemoteDataSourceImpl`: implement all 5 methods with correct service paths `/folder.v1.FolderService/{RpcName}` and updated request/response types (Req 4.2–4.6)

## Task 5: Update FolderRepository
- [x] 5.1 Update `FolderRepository` interface: replace `renameFolder` with `updateFolder`, add `getFolder` and `listFolders`, change return types — `createFolder` returns `Result<Folder>`, `updateFolder` returns `Result<Folder>`, `deleteFolder` returns `Result<Unit>` (Req 4.7–4.10)
- [x] 5.2 Update `FolderRepositoryImpl`: implement all 5 methods using updated mapper and data source, `deleteFolder` returns `Result.success(Unit)` (Req 4.8–4.10)

## Task 6: Update HomeViewModel for Domain Removal
- [x] 6.1 Update `HomeViewModel` to construct `CreateFolderParams` without domain field (Req 1.7)
- [x] 6.2 Update any `renameFolder` calls to use `updateFolder` with `UpdateFolderParams`
- [x] 6.3 Update any `deleteFolder` calls to use `DeleteFolderParams` without domain and handle `Result<Unit>` return

## Task 7: Update Note Proto Definition
- [ ] 7.1 Replace `proto/notes.proto` with the new proto definition: rename service to NoteService, wrap CreateNoteResponse/GetNoteResponse/UpdateNoteResponse in nested `note` field of type Note, make DeleteNoteResponse empty (Req 5)
- [ ] 7.2 Run Wire code generation

## Task 8: Update NoteMapper
- [ ] 8.1 Update `NoteMapper.toDomain(CreateNoteResponse)` to extract from nested `note` field (Req 6.1)
- [ ] 8.2 Update `NoteMapper.toDomain(GetNoteResponse)` to extract from nested `note` field (Req 6.2)
- [ ] 8.3 Add `NoteMapper.toDomain(UpdateNoteResponse)` to extract full Note from nested `note` field (Req 6.3)

## Task 9: Update NoteRemoteDataSourceImpl Service Path
- [ ] 9.1 Change all service paths in `NoteRemoteDataSourceImpl` from `/notes.v1.NotesService/` to `/notes.v1.NoteService/` (Req 7.1)

## Task 10: Update NotesRepositoryImpl for UpdateNote
- [ ] 10.1 Update `NotesRepositoryImpl.updateNote` to use the mapped Note directly from `UpdateNoteResponse` instead of issuing a separate `getNote` call (Req 6.5)
- [ ] 10.2 Update `NotesRepositoryImpl.syncPendingOperations` to also use the mapped Note from `UpdateNoteResponse` directly

## Task 11: Create TaskList Proto Definition
- [ ] 11.1 Create `proto/tasks.proto` with the TaskListService definition, SubTask, MainTask, and all request/response messages per Requirement 8
- [ ] 11.2 Run Wire code generation

## Task 12: Create TaskList Domain Models
- [ ] 12.1 Create `SubTask.kt` data class with `description: String` and `done: Boolean` (Req 9.1)
- [ ] 12.2 Create `MainTask.kt` data class with `description`, `done`, `dueDate`, `recurrence`, `subTasks: List<SubTask>` (Req 9.2)
- [ ] 12.3 Create `TaskList.kt` data class with `filePath`, `name`, `tasks: List<MainTask>`, `updatedAt` (Req 9.3)
- [ ] 12.4 Create `TaskListEntry.kt` data class with `filePath`, `name`, `updatedAt` (Req 9.4)
- [ ] 12.5 Create `ListTaskListsResult.kt` data class with `taskLists: List<TaskListEntry>` and `entries: List<String>` (Req 9.5)
- [ ] 12.6 Create `CreateTaskListParams.kt` data class with `name`, `path`, `tasks: List<MainTask>` (Req 9.6)
- [ ] 12.7 Create `UpdateTaskListParams.kt` data class with `filePath` and `tasks: List<MainTask>` (Req 9.7)

## Task 13: Create TaskListMapper
- [ ] 13.1 Create `TaskListMapper` object with proto→domain mappings for SubTask, MainTask, CreateTaskListResponse, GetTaskListResponse, ListTaskListsResponse, UpdateTaskListResponse (Req 10.1–10.6)
- [ ] 13.2 Add domain→proto mappings for CreateTaskListParams and UpdateTaskListParams including nested MainTask/SubTask conversion (Req 10.7–10.8)

## Task 14: Create TaskListRemoteDataSource
- [ ] 14.1 Create `TaskListRemoteDataSource` interface with createTaskList, getTaskList, listTaskLists, updateTaskList, deleteTaskList methods (Req 11.1)
- [ ] 14.2 Create `TaskListRemoteDataSourceImpl` using ConnectRpcClient with paths `/tasks.v1.TaskListService/{RpcName}` (Req 11.2–11.6)

## Task 15: Create TaskListRepository
- [ ] 15.1 Create `TaskListRepository` interface with all 5 CRUD methods and correct return types (Req 12.1–12.6)
- [ ] 15.2 Create `TaskListRepositoryImpl` following the same pattern as `FolderRepositoryImpl` — try/catch with `Result.success`/`Result.failure` (Req 12.7)

## Task 16: Update Koin DI Modules
- [ ] 16.1 Add `TaskListRemoteDataSource` → `TaskListRemoteDataSourceImpl` binding in `networkModule` (Req 13.1)
- [ ] 16.2 Add `TaskListRepository` → `TaskListRepositoryImpl` binding in `dataModule` (Req 13.2)
- [ ] 16.3 Verify existing `FolderRepository` and `FolderRemoteDataSource` bindings compile with updated types (Req 13.3–13.4)

## Task 17: Property-Based Tests — Folder Mapper
- [ ] 17.1 Write property test for FolderMapper domain→proto field preservation (Property 1): for any CreateFolderParams, UpdateFolderParams, DeleteFolderParams, mapping to proto preserves all fields
- [ ] 17.2 Write property test for FolderMapper proto→domain field preservation (Property 2): for any proto Folder, CreateFolderResponse, GetFolderResponse, ListFoldersResponse, UpdateFolderResponse, mapping to domain preserves path and name

## Task 18: Property-Based Tests — Note Mapper
- [ ] 18.1 Write property test for NoteMapper response→domain field preservation (Property 3): for any CreateNoteResponse, GetNoteResponse, UpdateNoteResponse with nested Note, mapping preserves all fields; for ListNotesResponse, mapping preserves notes count, entries count, and all field values

## Task 19: Property-Based Tests — TaskList Mapper
- [ ] 19.1 Write property test for TaskListMapper proto→domain field preservation (Property 4): for any proto MainTask with nested SubTasks, and any response type, mapping preserves all fields recursively
- [ ] 19.2 Write property test for TaskListMapper domain→proto field preservation (Property 5): for any CreateTaskListParams or UpdateTaskListParams with nested MainTask/SubTask, mapping preserves all fields
- [ ] 19.3 Write property test for TaskList mapping round-trip (Property 6): for any valid TaskList domain object, mapping to proto and back produces an equivalent object

## Task 20: Property-Based Tests — TaskListRepository Error Propagation
- [ ] 20.1 Write property test for TaskListRepository error propagation (Property 7): for any exception thrown by TaskListRemoteDataSource, every TaskListRepositoryImpl method returns Result.failure with that exact exception
