# Implementation Plan: Proto API Update

## Overview

Rename FolderService to FileService across proto definitions, data sources, mappers, repository, ViewModel, and DI. Update NoteService and TaskListService protos. Switch HomeViewModel from NotesRepository to FileRepository for directory listing. All changes flow top-down: proto → Wire config → network → mapper → repository → ViewModel → UI models → DI.

## Tasks

- [x] 1. Update proto definitions and Wire build config
  - [x] 1.1 Create `proto/file.proto` replacing `proto/folder.proto`
    - Define package `file.v1`, service `FileService` with `CreateFolder`, `ListFiles`, `UpdateFolder`, `DeleteFolder` RPCs
    - Define `Folder`, `CreateFolderRequest`, `CreateFolderResponse`, `ListFilesRequest`, `ListFilesResponse`, `UpdateFolderRequest`, `UpdateFolderResponse`, `DeleteFolderRequest`, `DeleteFolderResponse` messages
    - `ListFilesResponse` uses `repeated string entries` instead of typed Folder messages
    - Delete `proto/folder.proto`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12_

  - [x] 1.2 Update `proto/notes.proto` to match new backend contract
    - Update `Note` message with `file_path`, `title`, `content`, `updated_at` fields
    - Update `CreateNoteRequest` with `title`, `content`, `path` fields
    - Update `ListNotesResponse` to include `repeated string entries` alongside `repeated Note notes`
    - Update `UpdateNoteRequest` with `file_path` and `content` fields
    - Ensure all response messages match Requirement 7
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9_

  - [x] 1.3 Update `proto/tasks.proto` to match new backend contract
    - Rename service to `TaskListService` with `CreateTaskList`, `GetTaskList`, `ListTaskLists`, `UpdateTaskList`, `DeleteTaskList` RPCs
    - Update `SubTask` with `description` and `done` fields
    - Update `MainTask` with `description`, `done`, `due_date`, `recurrence`, `repeated sub_tasks` fields
    - Add `TaskListEntry` message with `file_path`, `name`, `updated_at`
    - Update `ListTaskListsResponse` to include `repeated string entries`
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_

  - [x] 1.4 Update Wire prune rule in `composeApp/build.gradle.kts`
    - Change `prune("folder.v1.FolderService")` to `prune("file.v1.FileService")`
    - _Requirements: 1.1, 1.2_

- [x] 2. Rename and update network layer (FileRemoteDataSource)
  - [x] 2.1 Create `FileRemoteDataSource` interface and `FileRemoteDataSourceImpl`
    - Rename `FolderRemoteDataSource.kt` → `FileRemoteDataSource.kt` with methods: `createFolder`, `listFiles`, `updateFolder`, `deleteFolder`
    - Remove `getFolder` method
    - Rename `FolderRemoteDataSourceImpl.kt` → `FileRemoteDataSourceImpl.kt`
    - Update all RPC paths to `/file.v1.FileService/*`
    - Use `file.v1` proto types for request/response serialization
    - Delete old `FolderRemoteDataSource.kt` and `FolderRemoteDataSourceImpl.kt`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

  - [x] 2.2 Write property test for FileRemoteDataSource RPC path correctness
    - **Property 3: FileRemoteDataSource RPC path correctness**
    - Create `FileRemoteDataSourcePropertyTest.kt` in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/data/source/`
    - Use a capturing fake `ConnectRpcClient` to verify each method calls the correct `/file.v1.FileService/{MethodName}` path
    - **Validates: Requirements 2.3, 2.4, 2.5, 2.6**

- [x] 3. Update mapper layer (FileMapper)
  - [x] 3.1 Rename `FolderMapper.kt` → `FileMapper.kt` and update mappings
    - Update all mappings to use `file.v1` proto types
    - Replace `toDomain(ListFoldersResponse)` with `toDomain(ListFilesResponse): List<String>` extracting entries
    - Keep `toDomain(Folder)`, `toDomain(CreateFolderResponse)`, `toDomain(UpdateFolderResponse)` updated for `file.v1` types
    - Keep `toProto` mappings for `CreateFolderParams`, `UpdateFolderParams`, `DeleteFolderParams` updated for `file.v1` types
    - Delete old `FolderMapper.kt`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 3.2 Write property tests for FileMapper field preservation
    - **Property 1: FileMapper domain-to-proto field preservation**
    - **Property 2: FileMapper proto-to-domain field preservation**
    - Create `FileMapperPropertyTest.kt` in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/data/mapper/`
    - Generate random domain params and proto messages, verify round-trip field preservation
    - **Validates: Requirements 1.4, 1.5, 1.6, 1.8, 1.9, 1.10, 1.11, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

- [x] 4. Checkpoint - Ensure proto, network, and mapper layers compile
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Rename and update repository layer (FileRepository)
  - [x] 5.1 Create `FileRepository` interface and `FileRepositoryImpl`
    - Rename `FolderRepository.kt` → `FileRepository.kt` with methods: `createFolder`, `listFiles`, `updateFolder`, `deleteFolder`
    - Remove `getFolder` method; `listFiles` returns `Result<List<String>>`
    - Rename `FolderRepositoryImpl.kt` → `FileRepositoryImpl.kt`
    - Delegate to `FileRemoteDataSource` + `FileMapper`
    - Wrap all calls in `try/catch` returning `Result.failure` on exception
    - Delete old `FolderRepository.kt` and `FolderRepositoryImpl.kt`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

  - [x] 5.2 Write property tests for FileRepositoryImpl
    - **Property 4: FileRepositoryImpl success delegation**
    - **Property 5: FileRepositoryImpl error propagation**
    - Create `FileRepositoryImplPropertyTest.kt` in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/data/repository/`
    - Use a fake data source; generate random inputs to verify success delegation and error propagation
    - **Validates: Requirements 4.3, 4.4, 4.5, 4.6, 4.7**

- [ ] 6. Update UI models and HomeViewModel
  - [ ] 6.1 Add `FileType` enum and update `FileUiModel` in `HomeScreenUiState.kt`
    - Add `enum class FileType { NOTE, TASK_LIST }`
    - Add `fileType: FileType` field to `FileUiModel`
    - _Requirements: 5.7, 10.3_

  - [ ] 6.2 Update `HomeViewModel` to use `FileRepository.listFiles`
    - Replace constructor dependency: `FileRepository` instead of `NotesRepository` + `FolderRepository`
    - Update `loadData()` to call `fileRepository.listFiles(path)`
    - Implement entry partitioning: folders (ending with `/`) vs files (not ending with `/`)
    - Implement file type detection: `note_` prefix → `NOTE`, `tasks_` prefix → `TASK_LIST`
    - Implement title derivation: strip `note_` or `tasks_` prefix from file names
    - Set `FolderUiModel.itemCount` to 0 (no sub-item count from string entries)
    - Set `FileUiModel.preview` and `FileUiModel.timestamp` to empty strings
    - Update `onInlineConfirm` to use `fileRepository.createFolder` and reload via `fileRepository.listFiles`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 10.1, 10.2, 10.3, 10.4_

  - [ ] 6.3 Write property tests for entry parsing logic
    - **Property 6: Entry partitioning into folders and files**
    - **Property 7: File entry type classification and title derivation**
    - **Property 8: Folder name extraction from path**
    - Create `EntryParsingPropertyTest.kt` in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/home/`
    - Generate random entry lists with mixed folder/file entries, verify partitioning, type classification, title derivation, and folder name extraction
    - **Validates: Requirements 5.4, 5.5, 5.6, 5.7, 10.3, 10.4**

  - [ ] 6.4 Write unit tests for HomeViewModel
    - Create `HomeViewModelTest.kt` in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/ui/home/`
    - Verify `loadData()` calls `fileRepository.listFiles`
    - Verify folder creation triggers reload via `fileRepository.listFiles`
    - Verify failure sets empty folders/files lists
    - _Requirements: 5.3, 5.8_

- [ ] 7. Update Koin DI modules
  - [ ] 7.1 Update `AppModules.kt` to wire renamed components
    - In `networkModule`: replace `FolderRemoteDataSource`/`FolderRemoteDataSourceImpl` binding with `FileRemoteDataSource`/`FileRemoteDataSourceImpl`
    - In `dataModule`: replace `FolderRepository`/`FolderRepositoryImpl` binding with `FileRepository`/`FileRepositoryImpl`
    - In `navigationModule`: update `HomeViewModel` constructor to inject `FileRepository` instead of `NotesRepository` + `FolderRepository`
    - Keep `NoteRemoteDataSource`, `NotesRepository`, `TaskListRemoteDataSource`, `TaskListRepository` bindings unchanged
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 9.1, 9.2, 9.3, 9.4_

  - [ ] 7.2 Write Koin module resolution test
    - Create `KoinModuleTest.kt` in `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/di/`
    - Verify DI resolution: `FileRemoteDataSource`, `FileRepository`, `HomeViewModel` injection
    - Verify preserved `NotesRepository` and `TaskListRepository` bindings still resolve
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 9.4_

- [ ] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Existing NoteService and TaskListService client logic (data sources, repositories, mappers) must be preserved — do not delete
- Each task references specific requirements for traceability
- Property tests use Kotest property (`io.kotest.property`) with `checkAll` and `PropTestConfig(iterations = 100)`
- Unit tests use Kotest `FunSpec` with hand-written fakes
