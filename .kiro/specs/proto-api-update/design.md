# Design Document: Proto API Update

## Overview

This design covers the update of EchoList's data layer to work with the new backend proto definitions. The backend API has evolved with new proto contracts for FileService, NoteService, and TaskListService. Since this is a pre-release update, no migration or backward compatibility is required.

The update follows a bottom-to-top approach:
1. FileService (folder operations)
2. NoteService (note CRUD operations)
3. TaskListService (task list CRUD operations)

Each service update involves three components:
- **Network Source**: ConnectRPC client implementation that makes RPC calls
- **Mapper**: Transforms proto messages to/from domain models
- **Repository**: Coordinates between network sources and UI layer

The domain models already match the new proto structure, so minimal changes are needed there. The primary work is updating the network layer to use the correct proto package names and ensuring all RPC methods are properly implemented.

## Architecture

### Layer Structure

```
UI Layer (ViewModels)
        ↓
Repository Layer (FileRepository, NotesRepository, TaskListRepository)
        ↓
Mapper Layer (FileMapper, NoteMapper, TaskListMapper)
        ↓
Network Source Layer (FileRemoteDataSource, NoteRemoteDataSource, TaskListRemoteDataSource)
        ↓
ConnectRPC Client (ConnectRpcClientImpl)
        ↓
Backend API (file.v1, notes.v1, tasks.v1)
```

### Service Package Names

The proto definitions use the following package structure:
- **FileService**: `file.v1` (handles folder operations)
- **NoteService**: `notes.v1` (handles note CRUD)
- **TaskListService**: `tasks.v1` (handles task list CRUD)
- **AuthService**: `auth.v1` (handles authentication)

### ConnectRPC Communication Pattern

All RPC calls follow this pattern:
1. Repository receives domain parameters
2. Mapper converts domain parameters to proto request messages
3. Network source calls ConnectRPC client with:
   - Service path (e.g., `/file.v1.FileService/CreateFolder`)
   - Request message
   - Request serializer (Wire ADAPTER.encode)
   - Response deserializer (Wire ADAPTER.decode)
4. Mapper converts proto response to domain model
5. Repository returns Result<T> to caller

## Components and Interfaces

### FileService Components

#### FileRemoteDataSource Interface
```kotlin
internal interface FileRemoteDataSource {
    suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse
    suspend fun listFiles(request: ListFilesRequest): ListFilesResponse
    suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse
    suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse
}
```

#### FileRemoteDataSourceImpl
- Uses ConnectRPC client to make RPC calls
- Service path format: `/file.v1.FileService/{MethodName}`
- Methods: CreateFolder, ListFiles, UpdateFolder, DeleteFolder
- Uses Wire ADAPTER for serialization/deserialization

#### FileMapper
Transformations:
- `file.v1.Folder` ↔ `Folder` domain model
- `CreateFolderParams` → `CreateFolderRequest`
- `CreateFolderResponse` → `Folder`
- `ListFilesResponse` → `List<String>`
- `UpdateFolderParams` → `UpdateFolderRequest`
- `UpdateFolderResponse` → `Folder`
- `DeleteFolderParams` → `DeleteFolderRequest`

#### FileRepository
- Orchestrates network calls through FileRemoteDataSource
- Uses FileMapper for all transformations
- Returns `Result<T>` for error handling
- Methods: createFolder, listFiles, updateFolder, deleteFolder

### NoteService Components

#### NoteRemoteDataSource Interface
```kotlin
internal interface NoteRemoteDataSource {
    suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse
    suspend fun listNotes(request: ListNotesRequest): ListNotesResponse
    suspend fun getNote(request: GetNoteRequest): GetNoteResponse
    suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse
    suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse
}
```

#### NoteRemoteDataSourceImpl
- Uses ConnectRPC client to make RPC calls
- Service path format: `/notes.v1.NoteService/{MethodName}`
- Methods: CreateNote, ListNotes, GetNote, UpdateNote, DeleteNote
- Uses Wire ADAPTER for serialization/deserialization

#### NoteMapper
Transformations:
- `notes.v1.Note` → `Note` domain model (with file_path, title, content, updated_at)
- `CreateNoteParams` → `CreateNoteRequest`
- `CreateNoteResponse` → `Note`
- `ListNotesResponse` → `ListNotesResult` (contains list of notes and entries)
- `GetNoteResponse` → `Note`
- `UpdateNoteParams` → `UpdateNoteRequest`
- `UpdateNoteResponse` → `Note`

#### NotesRepository
- Orchestrates network calls through NoteRemoteDataSource
- Uses NoteMapper for all transformations
- Returns `Result<T>` for error handling
- Methods: createNote, listNotes, getNote, updateNote, deleteNote

### TaskListService Components

#### TaskListRemoteDataSource Interface
```kotlin
internal interface TaskListRemoteDataSource {
    suspend fun createTaskList(request: CreateTaskListRequest): CreateTaskListResponse
    suspend fun getTaskList(request: GetTaskListRequest): GetTaskListResponse
    suspend fun listTaskLists(request: ListTaskListsRequest): ListTaskListsResponse
    suspend fun updateTaskList(request: UpdateTaskListRequest): UpdateTaskListResponse
    suspend fun deleteTaskList(request: DeleteTaskListRequest): DeleteTaskListResponse
}
```

#### TaskListRemoteDataSourceImpl
- Uses ConnectRPC client to make RPC calls
- Service path format: `/tasks.v1.TaskListService/{MethodName}`
- Methods: CreateTaskList, GetTaskList, ListTaskLists, UpdateTaskList, DeleteTaskList
- Uses Wire ADAPTER for serialization/deserialization

#### TaskListMapper
Transformations:
- `tasks.v1.MainTask` ↔ `MainTask` domain model (with description, done, due_date, recurrence, sub_tasks)
- `tasks.v1.SubTask` ↔ `SubTask` domain model (with description, done)
- `tasks.v1.TaskList` → `TaskList` domain model (with file_path, name, tasks, updated_at)
- `CreateTaskListParams` → `CreateTaskListRequest`
- `CreateTaskListResponse` → `TaskList`
- `GetTaskListResponse` → `TaskList`
- `ListTaskListsResponse` → `ListTaskListsResult`
- `UpdateTaskListParams` → `UpdateTaskListRequest`
- `UpdateTaskListResponse` → `TaskList`

#### TaskListRepository
- Orchestrates network calls through TaskListRemoteDataSource
- Uses TaskListMapper for all transformations
- Returns `Result<T>` for error handling
- Methods: createTaskList, getTaskList, listTaskLists, updateTaskList, deleteTaskList

### ConnectRPC Client Configuration

The ConnectRpcClientImpl is already properly configured and requires no changes:
- Uses Ktor HttpClient for HTTP communication
- Accepts dynamic NetworkConfig via NetworkConfigProvider
- Supports retry logic with configurable attempts and delays
- Handles serialization/deserialization via Wire ADAPTER
- Returns Result<T> for error handling
- Service paths are specified per-call (e.g., `/file.v1.FileService/CreateFolder`)

## Data Models

### Domain Models

The existing domain models already match the new proto structure:

**Folder**
```kotlin
data class Folder(
    val path: String,
    val name: String
)
```

**Note**
```kotlin
data class Note(
    val filePath: String,
    val title: String,
    val content: String,
    val updatedAt: Long // Unix timestamp in milliseconds
)
```

**TaskList**
```kotlin
data class TaskList(
    val filePath: String,
    val name: String,
    val tasks: List<MainTask>,
    val updatedAt: Long
)
```

**MainTask**
```kotlin
data class MainTask(
    val description: String,
    val done: Boolean,
    val dueDate: String,
    val recurrence: String,
    val subTasks: List<SubTask>
)
```

**SubTask**
```kotlin
data class SubTask(
    val description: String,
    val done: Boolean
)
```

### Proto Message Mapping

The mapper layer handles field name conversions between proto snake_case and Kotlin camelCase:

**Proto → Domain**
- `file_path` → `filePath`
- `updated_at` → `updatedAt`
- `due_date` → `dueDate`
- `sub_tasks` → `subTasks`
- `parent_path` → `parentPath`
- `folder_path` → `folderPath`
- `new_name` → `newName`
- `parent_dir` → `parentDir`

**Domain → Proto**
- `filePath` → `file_path`
- `updatedAt` → `updated_at`
- `dueDate` → `due_date`
- `subTasks` → `sub_tasks`
- `parentPath` → `parent_path`
- `folderPath` → `folder_path`
- `newName` → `new_name`
- `parentDir` → `parent_dir`

### Parameter Models

The existing parameter models (CreateFolderParams, UpdateFolderParams, etc.) remain unchanged as they already match the required request structure.


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: FileMapper transforms CreateFolderResponse correctly

*For any* valid CreateFolderResponse proto message, the FileMapper should transform it to a Folder domain model with matching path and name fields.

**Validates: Requirements 2.1**

### Property 2: FileMapper transforms ListFilesResponse correctly

*For any* valid ListFilesResponse proto message, the FileMapper should transform the entries list to a List<String> with all entries preserved in order.

**Validates: Requirements 2.2**

### Property 3: FileMapper transforms UpdateFolderResponse correctly

*For any* valid UpdateFolderResponse proto message, the FileMapper should transform it to a Folder domain model with matching path and name fields.

**Validates: Requirements 2.3**

### Property 4: FileRepository creates folders correctly

*For any* valid CreateFolderParams, the FileRepository should call the network source with the correctly mapped request and return a successful Result containing the mapped Folder.

**Validates: Requirements 3.1**

### Property 5: FileRepository lists files correctly

*For any* valid parent path string, the FileRepository should call the network source with the correct request and return a successful Result containing the mapped list of file entries.

**Validates: Requirements 3.2**

### Property 6: FileRepository updates folders correctly

*For any* valid UpdateFolderParams, the FileRepository should call the network source with the correctly mapped request and return a successful Result containing the mapped Folder.

**Validates: Requirements 3.3**

### Property 7: FileRepository deletes folders correctly

*For any* valid DeleteFolderParams, the FileRepository should call the network source with the correctly mapped request and return a successful Result.

**Validates: Requirements 3.4**

### Property 8: NoteMapper transforms Note proto messages correctly

*For any* valid Note proto message, the NoteMapper should transform it to a Note domain model with file_path mapped to filePath, and all other fields (title, content, updated_at) correctly mapped.

**Validates: Requirements 5.1**

### Property 9: NoteMapper transforms Note response messages correctly

*For any* valid response message containing a Note (CreateNoteResponse, GetNoteResponse, UpdateNoteResponse), the NoteMapper should extract and transform the Note to a domain model with all fields correctly mapped.

**Validates: Requirements 5.2, 5.4, 5.5**

### Property 10: NoteMapper transforms ListNotesResponse correctly

*For any* valid ListNotesResponse proto message with a list of notes, the NoteMapper should transform all notes in the repeated notes field to domain models, preserving order and count.

**Validates: Requirements 5.3**

### Property 11: NotesRepository creates notes correctly

*For any* valid CreateNoteParams with title, content, and parent_dir, the NotesRepository should call the network source with the correctly mapped request and return a successful Result containing the mapped Note.

**Validates: Requirements 6.1**

### Property 12: NotesRepository lists notes correctly

*For any* valid parent directory path, the NotesRepository should call the network source with the correct request and return a successful Result containing the mapped list of notes.

**Validates: Requirements 6.2**

### Property 13: NotesRepository gets notes correctly

*For any* valid file path, the NotesRepository should call the network source with the correct request and return a successful Result containing the mapped Note.

**Validates: Requirements 6.3**

### Property 14: NotesRepository updates notes correctly

*For any* valid UpdateNoteParams with file_path and content, the NotesRepository should call the network source with the correctly mapped request and return a successful Result containing the mapped Note.

**Validates: Requirements 6.4**

### Property 15: NotesRepository deletes notes correctly

*For any* valid file path, the NotesRepository should call the network source with the correct request and return a successful Result.

**Validates: Requirements 6.5**

### Property 16: TaskListMapper transforms MainTask proto messages correctly

*For any* valid MainTask proto message, the TaskListMapper should transform it to a MainTask domain model with all fields (description, done, due_date, recurrence, sub_tasks) correctly mapped.

**Validates: Requirements 9.1**

### Property 17: TaskListMapper transforms SubTask proto messages correctly

*For any* valid SubTask proto message, the TaskListMapper should transform it to a SubTask domain model with description and done fields correctly mapped.

**Validates: Requirements 9.2**

### Property 18: TaskListMapper transforms TaskList response messages correctly

*For any* valid response message containing a TaskList (CreateTaskListResponse, GetTaskListResponse, UpdateTaskListResponse), the TaskListMapper should extract and transform the TaskList to a domain model with all fields correctly mapped.

**Validates: Requirements 9.3, 9.4, 9.6**

### Property 19: TaskListMapper transforms ListTaskListsResponse correctly

*For any* valid ListTaskListsResponse proto message, the TaskListMapper should transform both task_lists and entries fields to domain models, preserving all data.

**Validates: Requirements 9.5**

### Property 20: TaskListMapper round-trip transformation preserves data

*For any* valid MainTask or SubTask domain model, transforming it to proto and back to domain should produce an equivalent model (round-trip property).

**Validates: Requirements 9.7**

### Property 21: TaskListRepository creates task lists correctly

*For any* valid CreateTaskListParams with name, path, and tasks, the TaskListRepository should call the network source with the correctly mapped request and return a successful Result containing the mapped TaskList.

**Validates: Requirements 10.1**

### Property 22: TaskListRepository gets task lists correctly

*For any* valid file path, the TaskListRepository should call the network source with the correct request and return a successful Result containing the mapped TaskList.

**Validates: Requirements 10.2**

### Property 23: TaskListRepository lists task lists correctly

*For any* valid path, the TaskListRepository should call the network source with the correct request and return a successful Result containing the mapped list of task lists.

**Validates: Requirements 10.3**

### Property 24: TaskListRepository updates task lists correctly

*For any* valid UpdateTaskListParams with file_path and tasks, the TaskListRepository should call the network source with the correctly mapped request and return a successful Result containing the mapped TaskList.

**Validates: Requirements 10.4**

### Property 25: TaskListRepository deletes task lists correctly

*For any* valid file path, the TaskListRepository should call the network source with the correct request and return a successful Result.

**Validates: Requirements 10.5**

## Error Handling

### Network Layer Error Handling

The ConnectRPC client already implements comprehensive error handling:
- **Serialization errors**: Caught and wrapped in NetworkException.SerializationError
- **Client errors (4xx)**: Wrapped in NetworkException.ClientError with status code and message
- **Server errors (5xx)**: Wrapped in NetworkException.ServerError with status code and message
- **Timeout errors**: Caught and wrapped in NetworkException.TimeoutError
- **Network errors**: Caught and wrapped in NetworkException.NetworkError

### Repository Layer Error Handling

All repository methods follow the same error handling pattern:
1. Wrap network calls in try-catch blocks
2. Return `Result.success(T)` for successful operations
3. Return `Result.failure(Exception)` for any errors
4. Execute on a background dispatcher (Dispatchers.Default)

This pattern ensures:
- Exceptions never propagate to the UI layer uncaught
- Callers can handle errors using Result's functional API
- All operations are non-blocking

### Mapper Layer Error Handling

Mappers assume valid proto messages and will throw exceptions for:
- Null required fields (e.g., `proto.folder!!` will throw if null)
- Invalid data types

These exceptions are caught by the repository layer and returned as Result.failure.

### Error Recovery

The ConnectRPC client supports automatic retry with configurable:
- Maximum retry attempts (from NetworkConfig.maxRetries)
- Retry delay in milliseconds (from NetworkConfig.retryDelayMs)

Retries are attempted for transient failures but not for:
- Client errors (4xx) - these indicate invalid requests
- Serialization errors - these indicate code bugs

## Testing Strategy

### Dual Testing Approach

This feature requires both unit tests and property-based tests:

**Unit Tests** focus on:
- Specific examples of proto message transformations
- Edge cases (empty lists, null optional fields)
- Error conditions (network failures, invalid responses)
- Integration points between components

**Property-Based Tests** focus on:
- Universal properties that hold for all inputs
- Comprehensive input coverage through randomization
- Mapper transformations across all valid proto messages
- Repository integration across all valid parameters

### Property-Based Testing Configuration

**Framework**: Kotest property-based testing (kotest-property)

**Configuration**:
- Minimum 100 iterations per property test
- Each test must reference its design document property
- Tag format: `Feature: proto-api-update, Property {number}: {property_text}`

**Test Organization**:
```
composeApp/src/commonTest/kotlin/net/onefivefour/echolist/
├── data/
│   ├── mapper/
│   │   ├── FileMapperTest.kt (unit tests)
│   │   ├── FileMapperPropertyTest.kt (properties 1-3)
│   │   ├── NoteMapperTest.kt (unit tests)
│   │   ├── NoteMapperPropertyTest.kt (properties 8-10)
│   │   ├── TaskListMapperTest.kt (unit tests)
│   │   └── TaskListMapperPropertyTest.kt (properties 16-20)
│   └── repository/
│       ├── FileRepositoryTest.kt (unit tests)
│       ├── FileRepositoryPropertyTest.kt (properties 4-7)
│       ├── NotesRepositoryTest.kt (unit tests)
│       ├── NotesRepositoryPropertyTest.kt (properties 11-15)
│       ├── TaskListRepositoryTest.kt (unit tests)
│       └── TaskListRepositoryPropertyTest.kt (properties 21-25)
```

### Property Test Generators

Property tests require custom generators for proto messages:

**FileMapper Generators**:
- `Arb.createFolderResponse()`: Generates random CreateFolderResponse with valid Folder
- `Arb.listFilesResponse()`: Generates random ListFilesResponse with 0-100 entries
- `Arb.updateFolderResponse()`: Generates random UpdateFolderResponse with valid Folder
- `Arb.folder()`: Generates random Folder proto with valid path and name

**NoteMapper Generators**:
- `Arb.note()`: Generates random Note proto with all required fields
- `Arb.createNoteResponse()`: Generates random CreateNoteResponse with valid Note
- `Arb.getNoteResponse()`: Generates random GetNoteResponse with valid Note
- `Arb.updateNoteResponse()`: Generates random UpdateNoteResponse with valid Note
- `Arb.listNotesResponse()`: Generates random ListNotesResponse with 0-100 notes

**TaskListMapper Generators**:
- `Arb.mainTask()`: Generates random MainTask proto with 0-10 subtasks
- `Arb.subTask()`: Generates random SubTask proto
- `Arb.taskList()`: Generates random TaskList proto with 0-20 main tasks
- `Arb.createTaskListResponse()`: Generates random CreateTaskListResponse
- `Arb.getTaskListResponse()`: Generates random GetTaskListResponse
- `Arb.updateTaskListResponse()`: Generates random UpdateTaskListResponse
- `Arb.listTaskListsResponse()`: Generates random ListTaskListsResponse

**Repository Generators**:
- `Arb.createFolderParams()`: Generates random CreateFolderParams
- `Arb.updateFolderParams()`: Generates random UpdateFolderParams
- `Arb.deleteFolderParams()`: Generates random DeleteFolderParams
- `Arb.createNoteParams()`: Generates random CreateNoteParams
- `Arb.updateNoteParams()`: Generates random UpdateNoteParams
- `Arb.createTaskListParams()`: Generates random CreateTaskListParams
- `Arb.updateTaskListParams()`: Generates random UpdateTaskListParams

### Unit Test Coverage

Unit tests should cover:

**Mapper Tests**:
- Specific examples of each transformation
- Empty lists and collections
- Null optional fields (if any)
- Field name conversions (snake_case ↔ camelCase)

**Repository Tests**:
- Successful operations with mock network sources
- Network failures and error handling
- Result wrapping (success and failure cases)
- Dispatcher usage verification

**Integration Tests**:
- End-to-end flows with real ConnectRPC client (against test server)
- Service path verification (correct package names)
- Request/response serialization
- Authentication header propagation

### Test Doubles

**Mock Network Sources**:
- FileRemoteDataSourceFake: Returns predefined responses for testing
- NoteRemoteDataSourceFake: Returns predefined responses for testing
- TaskListRemoteDataSourceFake: Returns predefined responses for testing

These fakes are used in repository tests to isolate the repository logic from network concerns.

### Continuous Integration

All tests (unit and property-based) must pass before merging:
- Run on every commit
- Run on all target platforms (Android, iOS, JVM, JS, WasmJS)
- Property tests run with 100 iterations minimum
- Code coverage target: 80% for mapper and repository layers
