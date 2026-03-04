# Requirements Document

## Introduction

The backend API has evolved with new proto definitions for all services. The EchoList client application needs to be updated to work with the new API contracts. This is a pre-release update, so no migration or backward compatibility is required. The update will proceed bottom-to-top: file service first, then notes and tasks services.

## Glossary

- **Proto_File**: Protocol Buffer definition file (.proto) that defines service contracts and message types
- **FileService**: Backend service for folder operations (previously named "folder service")
- **NoteService**: Backend service for note CRUD operations
- **TaskListService**: Backend service for task list CRUD operations
- **AuthService**: Backend service for authentication operations
- **Network_Source**: Kotlin client implementation that calls the backend service via ConnectRPC
- **Repository**: Data layer component that coordinates between network sources and UI layer
- **Mapper**: Component that transforms proto messages to domain models
- **go_package**: Proto option that specifies the Go package path for generated code
- **ConnectRPC**: RPC framework used for client-server communication

## Requirements

### Requirement 1: Update FileService Network Source

**User Story:** As a developer, I want the FileService network source updated to match the new proto definition, so that folder operations work with the backend API.

#### Acceptance Criteria

1. THE FileService_Network_Source SHALL implement CreateFolder RPC with CreateFolderRequest and CreateFolderResponse
2. THE FileService_Network_Source SHALL implement ListFiles RPC with ListFilesRequest and ListFilesResponse
3. THE FileService_Network_Source SHALL implement UpdateFolder RPC with UpdateFolderRequest and UpdateFolderResponse
4. THE FileService_Network_Source SHALL implement DeleteFolder RPC with DeleteFolderRequest and DeleteFolderResponse
5. THE FileService_Network_Source SHALL use the package name "file.v1"

### Requirement 2: Update FileService Mapper

**User Story:** As a developer, I want the FileMapper updated to transform new proto messages, so that the data layer can work with domain models.

#### Acceptance Criteria

1. WHEN a CreateFolderResponse is received, THE FileMapper SHALL transform it to a Folder domain model
2. WHEN a ListFilesResponse is received, THE FileMapper SHALL transform the entries list to domain models
3. WHEN an UpdateFolderResponse is received, THE FileMapper SHALL transform it to a Folder domain model
4. THE FileMapper SHALL handle the new Folder message structure with path and name fields

### Requirement 3: Update FileRepository

**User Story:** As a developer, I want the FileRepository updated to use the new network source methods, so that the UI layer can perform folder operations.

#### Acceptance Criteria

1. THE FileRepository SHALL call the updated CreateFolder method with correct parameters
2. THE FileRepository SHALL call the updated ListFiles method with correct parameters
3. THE FileRepository SHALL call the updated UpdateFolder method with correct parameters
4. THE FileRepository SHALL call the updated DeleteFolder method with correct parameters
5. THE FileRepository SHALL use the updated mapper to transform responses

### Requirement 4: Update NoteService Network Source

**User Story:** As a developer, I want the NoteService network source updated to match the new proto definition, so that note operations work with the backend API.

#### Acceptance Criteria

1. THE NoteService_Network_Source SHALL implement CreateNote RPC with CreateNoteRequest and CreateNoteResponse
2. THE NoteService_Network_Source SHALL implement ListNotes RPC with ListNotesRequest and ListNotesResponse
3. THE NoteService_Network_Source SHALL implement GetNote RPC with GetNoteRequest and GetNoteResponse
4. THE NoteService_Network_Source SHALL implement UpdateNote RPC with UpdateNoteRequest and UpdateNoteResponse
5. THE NoteService_Network_Source SHALL implement DeleteNote RPC with DeleteNoteRequest and DeleteNoteResponse
6. THE NoteService_Network_Source SHALL use the package name "notes.v1"

### Requirement 5: Update NoteService Mapper

**User Story:** As a developer, I want the NoteMapper updated to transform new proto messages, so that the data layer can work with domain models.

#### Acceptance Criteria

1. WHEN a Note proto message is received, THE NoteMapper SHALL transform it to a Note domain model with file_path, title, content, and updated_at fields
2. WHEN a CreateNoteResponse is received, THE NoteMapper SHALL extract and transform the Note message
3. WHEN a ListNotesResponse is received, THE NoteMapper SHALL transform the repeated notes field
4. WHEN a GetNoteResponse is received, THE NoteMapper SHALL extract and transform the Note message
5. WHEN a UpdateNoteResponse is received, THE NoteMapper SHALL extract and transform the Note message

### Requirement 6: Update NotesRepository

**User Story:** As a developer, I want the NotesRepository updated to use the new network source methods, so that the UI layer can perform note operations.

#### Acceptance Criteria

1. THE NotesRepository SHALL call the updated CreateNote method with title, content, and parent_dir parameters
2. THE NotesRepository SHALL call the updated ListNotes method with parent_dir parameter
3. THE NotesRepository SHALL call the updated GetNote method with file_path parameter
4. THE NotesRepository SHALL call the updated UpdateNote method with file_path and content parameters
5. THE NotesRepository SHALL call the updated DeleteNote method with file_path parameter
6. THE NotesRepository SHALL use the updated mapper to transform responses

### Requirement 7: Update TaskListService Network Source

**User Story:** As a developer, I want the TaskListService network source updated to match the new proto definition, so that task list operations work with the backend API.

#### Acceptance Criteria

1. THE TaskListService_Network_Source SHALL implement CreateTaskList RPC with CreateTaskListRequest and CreateTaskListResponse
2. THE TaskListService_Network_Source SHALL implement GetTaskList RPC with GetTaskListRequest and GetTaskListResponse
3. THE TaskListService_Network_Source SHALL implement ListTaskLists RPC with ListTaskListsRequest and ListTaskListsResponse
4. THE TaskListService_Network_Source SHALL implement UpdateTaskList RPC with UpdateTaskListRequest and UpdateTaskListResponse
5. THE TaskListService_Network_Source SHALL implement DeleteTaskList RPC with DeleteTaskListRequest and DeleteTaskListResponse
6. THE TaskListService_Network_Source SHALL use the package name "tasks.v1"

### Requirement 9: Update TaskListService Mapper

**User Story:** As a developer, I want the TaskListMapper updated to transform new proto messages, so that the data layer can work with domain models.

#### Acceptance Criteria

1. WHEN a MainTask proto message is received, THE TaskListMapper SHALL transform it to a MainTask domain model with description, done, due_date, recurrence, and sub_tasks fields
2. WHEN a SubTask proto message is received, THE TaskListMapper SHALL transform it to a SubTask domain model with description and done fields
3. WHEN a CreateTaskListResponse is received, THE TaskListMapper SHALL transform it to a TaskList domain model
4. WHEN a GetTaskListResponse is received, THE TaskListMapper SHALL transform it to a TaskList domain model
5. WHEN a ListTaskListsResponse is received, THE TaskListMapper SHALL transform task_lists and entries fields
6. WHEN a UpdateTaskListResponse is received, THE TaskListMapper SHALL transform it to a TaskList domain model
7. WHEN creating proto messages from domain models, THE TaskListMapper SHALL transform MainTask and SubTask domain models to proto messages

### Requirement 10: Update TaskListRepository

**User Story:** As a developer, I want the TaskListRepository updated to use the new network source methods, so that the UI layer can perform task list operations.

#### Acceptance Criteria

1. THE TaskListRepository SHALL call the updated CreateTaskList method with name, path, and tasks parameters
2. THE TaskListRepository SHALL call the updated GetTaskList method with file_path parameter
3. THE TaskListRepository SHALL call the updated ListTaskLists method with path parameter
4. THE TaskListRepository SHALL call the updated UpdateTaskList method with file_path and tasks parameters
5. THE TaskListRepository SHALL call the updated DeleteTaskList method with file_path parameter
6. THE TaskListRepository SHALL use the updated mapper to transform responses

### Requirement 11: Update Domain Models

**User Story:** As a developer, I want domain models updated to match the new proto message structures, so that the application can represent all data correctly.

#### Acceptance Criteria

1. WHERE the proto message structure differs from the current domain model, THE domain model SHALL be updated to match
2. THE Note domain model SHALL include file_path, title, content, and updated_at fields
3. THE TaskList domain model SHALL include file_path, name, tasks, and updated_at fields
4. THE MainTask domain model SHALL include description, done, due_date, recurrence, and sub_tasks fields
5. THE SubTask domain model SHALL include description and done fields
6. THE Folder domain model SHALL include path and name fields

### Requirement 12: Verify ConnectRPC Client Configuration

**User Story:** As a developer, I want to verify the ConnectRPC client is configured correctly for all services, so that RPC calls can be made successfully.

#### Acceptance Criteria

1. THE ConnectRPC_Client SHALL be configured to call FileService at the correct endpoint
2. THE ConnectRPC_Client SHALL be configured to call NoteService at the correct endpoint
3. THE ConnectRPC_Client SHALL be configured to call TaskListService at the correct endpoint
4. THE ConnectRPC_Client SHALL be configured to call AuthService at the correct endpoint
5. THE ConnectRPC_Client SHALL use the correct package names for all service calls
