# Implementation Plan: Proto API Update

## Overview

This plan implements updates to EchoList's data layer to work with the new backend proto definitions. The implementation follows a bottom-to-top approach: FileService first, then NoteService, then TaskListService. Each service update involves updating the network source (RemoteDataSource), mapper (proto ↔ domain transformations), and repository (orchestration layer).

## Tasks

- [x] 1. Update FileService network source and mapper
  - [x] 1.1 Update FileRemoteDataSourceImpl to use new proto package
    - Update service path to `/file.v1.FileService/{MethodName}` for all RPC calls
    - Implement CreateFolder, ListFiles, UpdateFolder, DeleteFolder methods
    - Use Wire ADAPTER for serialization/deserialization
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  
  - [x] 1.2 Update FileMapper for new proto messages
    - Implement transformation from CreateFolderResponse to Folder domain model
    - Implement transformation from ListFilesResponse to List<String>
    - Implement transformation from UpdateFolderResponse to Folder domain model
    - Handle Folder message structure with path and name fields
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [x] 1.3 Write unit tests for FileMapper
    - Test specific examples of each transformation
    - Test empty lists and edge cases
    - Test field name conversions (snake_case ↔ camelCase)
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [x] 1.4 Write property test for FileMapper CreateFolderResponse transformation
    - **Property 1: FileMapper transforms CreateFolderResponse correctly**
    - **Validates: Requirements 2.1**
    - Create Arb.createFolderResponse() and Arb.folder() generators
    - Verify path and name fields match after transformation
    - Run with minimum 100 iterations
  
  - [x] 1.5 Write property test for FileMapper ListFilesResponse transformation
    - **Property 2: FileMapper transforms ListFilesResponse correctly**
    - **Validates: Requirements 2.2**
    - Create Arb.listFilesResponse() generator (0-100 entries)
    - Verify all entries preserved in order
    - Run with minimum 100 iterations
  
  - [x] 1.6 Write property test for FileMapper UpdateFolderResponse transformation
    - **Property 3: FileMapper transforms UpdateFolderResponse correctly**
    - **Validates: Requirements 2.3**
    - Create Arb.updateFolderResponse() generator
    - Verify path and name fields match after transformation
    - Run with minimum 100 iterations

- [x] 2. Update FileRepository and integration
  - [x] 2.1 Update FileRepository to use new network source methods
    - Update createFolder to call updated network source with correct parameters
    - Update listFiles to call updated network source with correct parameters
    - Update updateFolder to call updated network source with correct parameters
    - Update deleteFolder to call updated network source with correct parameters
    - Use updated FileMapper for all transformations
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 2.2 Write unit tests for FileRepository
    - Test successful operations with mock network source
    - Test network failures and error handling
    - Test Result wrapping (success and failure cases)
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [x] 2.3 Write property test for FileRepository createFolder
    - **Property 4: FileRepository creates folders correctly**
    - **Validates: Requirements 3.1**
    - Create Arb.createFolderParams() generator
    - Verify correct request mapping and successful Result
    - Run with minimum 100 iterations
  
  - [x] 2.4 Write property test for FileRepository listFiles
    - **Property 5: FileRepository lists files correctly**
    - **Validates: Requirements 3.2**
    - Generate random parent path strings
    - Verify correct request and successful Result with mapped entries
    - Run with minimum 100 iterations
  
  - [x] 2.5 Write property test for FileRepository updateFolder
    - **Property 6: FileRepository updates folders correctly**
    - **Validates: Requirements 3.3**
    - Create Arb.updateFolderParams() generator
    - Verify correct request mapping and successful Result
    - Run with minimum 100 iterations
  
  - [x] 2.6 Write property test for FileRepository deleteFolder
    - **Property 7: FileRepository deletes folders correctly**
    - **Validates: Requirements 3.4**
    - Create Arb.deleteFolderParams() generator
    - Verify correct request and successful Result
    - Run with minimum 100 iterations

- [x] 3. Checkpoint - Verify FileService updates
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Update NoteService network source and mapper
  - [x] 4.1 Update NoteRemoteDataSourceImpl to use new proto package
    - Update service path to `/notes.v1.NoteService/{MethodName}` for all RPC calls
    - Implement CreateNote, ListNotes, GetNote, UpdateNote, DeleteNote methods
    - Use Wire ADAPTER for serialization/deserialization
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_
  
  - [x] 4.2 Update NoteMapper for new proto messages
    - Implement transformation from Note proto to Note domain model (file_path, title, content, updated_at)
    - Implement transformation from CreateNoteResponse to Note domain model
    - Implement transformation from ListNotesResponse to list of Note domain models
    - Implement transformation from GetNoteResponse to Note domain model
    - Implement transformation from UpdateNoteResponse to Note domain model
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 4.3 Write unit tests for NoteMapper
    - Test specific examples of each transformation
    - Test empty lists and edge cases
    - Test field name conversions (file_path → filePath, updated_at → updatedAt)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 4.4 Write property test for NoteMapper Note proto transformation
    - **Property 8: NoteMapper transforms Note proto messages correctly**
    - **Validates: Requirements 5.1**
    - Create Arb.note() generator
    - Verify file_path → filePath and all other fields correctly mapped
    - Run with minimum 100 iterations
  
  - [x] 4.5 Write property test for NoteMapper response transformations
    - **Property 9: NoteMapper transforms Note response messages correctly**
    - **Validates: Requirements 5.2, 5.4, 5.5**
    - Create Arb.createNoteResponse(), Arb.getNoteResponse(), Arb.updateNoteResponse() generators
    - Verify Note extraction and field mapping for all response types
    - Run with minimum 100 iterations
  
  - [x] 4.6 Write property test for NoteMapper ListNotesResponse transformation
    - **Property 10: NoteMapper transforms ListNotesResponse correctly**
    - **Validates: Requirements 5.3**
    - Create Arb.listNotesResponse() generator (0-100 notes)
    - Verify all notes transformed, preserving order and count
    - Run with minimum 100 iterations

- [x] 5. Update NotesRepository and integration
  - [x] 5.1 Update NotesRepository to use new network source methods
    - Update createNote to call network source with title, content, parent_dir parameters
    - Update listNotes to call network source with parent_dir parameter
    - Update getNote to call network source with file_path parameter
    - Update updateNote to call network source with file_path and content parameters
    - Update deleteNote to call network source with file_path parameter
    - Use updated NoteMapper for all transformations
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_
  
  - [x] 5.2 Write unit tests for NotesRepository
    - Test successful operations with mock network source
    - Test network failures and error handling
    - Test Result wrapping (success and failure cases)
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [x] 5.3 Write property test for NotesRepository createNote
    - **Property 11: NotesRepository creates notes correctly**
    - **Validates: Requirements 6.1**
    - Create Arb.createNoteParams() generator
    - Verify correct request mapping and successful Result
    - Run with minimum 100 iterations
  
  - [x] 5.4 Write property test for NotesRepository listNotes
    - **Property 12: NotesRepository lists notes correctly**
    - **Validates: Requirements 6.2**
    - Generate random parent directory paths
    - Verify correct request and successful Result with mapped notes
    - Run with minimum 100 iterations
  
  - [x] 5.5 Write property test for NotesRepository getNote
    - **Property 13: NotesRepository gets notes correctly**
    - **Validates: Requirements 6.3**
    - Generate random file paths
    - Verify correct request and successful Result with mapped Note
    - Run with minimum 100 iterations
  
  - [x] 5.6 Write property test for NotesRepository updateNote
    - **Property 14: NotesRepository updates notes correctly**
    - **Validates: Requirements 6.4**
    - Create Arb.updateNoteParams() generator
    - Verify correct request mapping and successful Result
    - Run with minimum 100 iterations
  
  - [x] 5.7 Write property test for NotesRepository deleteNote
    - **Property 15: NotesRepository deletes notes correctly**
    - **Validates: Requirements 6.5**
    - Generate random file paths
    - Verify correct request and successful Result
    - Run with minimum 100 iterations

- [x] 6. Checkpoint - Verify NoteService updates
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Update TaskListService network source and mapper
  - [x] 7.1 Update TaskListRemoteDataSourceImpl to use new proto package
    - Update service path to `/tasks.v1.TaskListService/{MethodName}` for all RPC calls
    - Implement CreateTaskList, GetTaskList, ListTaskLists, UpdateTaskList, DeleteTaskList methods
    - Use Wire ADAPTER for serialization/deserialization
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  
  - [x] 7.2 Update TaskListMapper for new proto messages
    - Implement transformation from MainTask proto to MainTask domain model (description, done, due_date, recurrence, sub_tasks)
    - Implement transformation from SubTask proto to SubTask domain model (description, done)
    - Implement transformation from CreateTaskListResponse to TaskList domain model
    - Implement transformation from GetTaskListResponse to TaskList domain model
    - Implement transformation from ListTaskListsResponse to domain models (task_lists and entries)
    - Implement transformation from UpdateTaskListResponse to TaskList domain model
    - Implement transformation from MainTask/SubTask domain models to proto messages
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_
  
  - [x] 7.3 Write unit tests for TaskListMapper
    - Test specific examples of each transformation
    - Test empty lists and edge cases
    - Test field name conversions (due_date → dueDate, sub_tasks → subTasks)
    - Test bidirectional transformations (domain → proto → domain)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_
  
  - [x] 7.4 Write property test for TaskListMapper MainTask transformation
    - **Property 16: TaskListMapper transforms MainTask proto messages correctly**
    - **Validates: Requirements 9.1**
    - Create Arb.mainTask() generator (0-10 subtasks)
    - Verify all fields correctly mapped (description, done, due_date, recurrence, sub_tasks)
    - Run with minimum 100 iterations
  
  - [x] 7.5 Write property test for TaskListMapper SubTask transformation
    - **Property 17: TaskListMapper transforms SubTask proto messages correctly**
    - **Validates: Requirements 9.2**
    - Create Arb.subTask() generator
    - Verify description and done fields correctly mapped
    - Run with minimum 100 iterations
  
  - [x] 7.6 Write property test for TaskListMapper response transformations
    - **Property 18: TaskListMapper transforms TaskList response messages correctly**
    - **Validates: Requirements 9.3, 9.4, 9.6**
    - Create Arb.createTaskListResponse(), Arb.getTaskListResponse(), Arb.updateTaskListResponse() generators
    - Verify TaskList extraction and field mapping for all response types
    - Run with minimum 100 iterations
  
  - [x] 7.7 Write property test for TaskListMapper ListTaskListsResponse transformation
    - **Property 19: TaskListMapper transforms ListTaskListsResponse correctly**
    - **Validates: Requirements 9.5**
    - Create Arb.listTaskListsResponse() generator
    - Verify both task_lists and entries fields transformed correctly
    - Run with minimum 100 iterations
  
  - [x] 7.8 Write property test for TaskListMapper round-trip transformation
    - **Property 20: TaskListMapper round-trip transformation preserves data**
    - **Validates: Requirements 9.7**
    - Generate random MainTask and SubTask domain models
    - Transform to proto and back to domain
    - Verify equivalence (round-trip property)
    - Run with minimum 100 iterations

- [ ] 8. Update TaskListRepository and integration
  - [ ] 8.1 Update TaskListRepository to use new network source methods
    - Update createTaskList to call network source with name, path, tasks parameters
    - Update getTaskList to call network source with file_path parameter
    - Update listTaskLists to call network source with path parameter
    - Update updateTaskList to call network source with file_path and tasks parameters
    - Update deleteTaskList to call network source with file_path parameter
    - Use updated TaskListMapper for all transformations
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_
  
  - [ ] 8.2 Write unit tests for TaskListRepository
    - Test successful operations with mock network source
    - Test network failures and error handling
    - Test Result wrapping (success and failure cases)
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [ ] 8.3 Write property test for TaskListRepository createTaskList
    - **Property 21: TaskListRepository creates task lists correctly**
    - **Validates: Requirements 10.1**
    - Create Arb.createTaskListParams() generator
    - Verify correct request mapping and successful Result
    - Run with minimum 100 iterations
  
  - [ ] 8.4 Write property test for TaskListRepository getTaskList
    - **Property 22: TaskListRepository gets task lists correctly**
    - **Validates: Requirements 10.2**
    - Generate random file paths
    - Verify correct request and successful Result with mapped TaskList
    - Run with minimum 100 iterations
  
  - [ ] 8.5 Write property test for TaskListRepository listTaskLists
    - **Property 23: TaskListRepository lists task lists correctly**
    - **Validates: Requirements 10.3**
    - Generate random paths
    - Verify correct request and successful Result with mapped list
    - Run with minimum 100 iterations
  
  - [ ] 8.6 Write property test for TaskListRepository updateTaskList
    - **Property 24: TaskListRepository updates task lists correctly**
    - **Validates: Requirements 10.4**
    - Create Arb.updateTaskListParams() generator
    - Verify correct request mapping and successful Result
    - Run with minimum 100 iterations
  
  - [ ] 8.7 Write property test for TaskListRepository deleteTaskList
    - **Property 25: TaskListRepository deletes task lists correctly**
    - **Validates: Requirements 10.5**
    - Generate random file paths
    - Verify correct request and successful Result
    - Run with minimum 100 iterations

- [ ] 9. Final checkpoint - Verify all service updates
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after each service update
- Property tests validate universal correctness properties with minimum 100 iterations
- Unit tests validate specific examples and edge cases
- All property tests must be tagged with: `Feature: proto-api-update, Property {number}: {property_text}`
- Test organization follows the structure: mapper tests and repository tests in separate files
- Property-based tests use Kotest property testing framework (kotest-property)
