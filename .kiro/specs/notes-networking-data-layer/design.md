# Design Document: Notes Networking and Data Layer

## Overview

This design implements a cross-platform networking and data layer for a Kotlin Multiplatform notes application that communicates with a Go backend using ConnectRPC over protobuf. The architecture follows clean architecture principles with clear separation between network, data, and domain layers.

The solution uses:
- **Wire** for protobuf code generation (KMP-compatible)
- **Ktor Client** for HTTP networking (supports all KMP targets)
- **Custom ConnectRPC implementation** for protocol handling
- **SQLDelight** for local caching and offline support
- **Repository pattern** for data access abstraction

## Architecture

### Layer Structure

```
┌─────────────────────────────────────┐
│         Domain Layer                │
│  (Data Models, Repository Interface)│
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│         Data Layer                  │
│  (Repository Implementation)        │
│  ┌──────────────┐  ┌─────────────┐ │
│  │ Network      │  │ Cache       │ │
│  │ Data Source  │  │ Data Source │ │
│  └──────────────┘  └─────────────┘ │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│      Infrastructure Layer           │
│  ┌──────────────┐  ┌─────────────┐ │
│  │ ConnectRPC   │  │ SQLDelight  │ │
│  │ Client       │  │ Database    │ │
│  └──────────────┘  └─────────────┘ │
└─────────────────────────────────────┘
```

### Module Organization

```
composeApp/src/
  commonMain/kotlin/
    net/onefivefour/notes/
      data/
        models/          # Domain data models
        repository/      # Repository interface and implementation
        source/
          network/       # Network data source
          cache/         # Cache data source
        mapper/          # Proto <-> Domain mappers
      network/
        client/          # ConnectRPC client
        config/          # Network configuration
        error/           # Error types
      proto/             # Generated protobuf code (Wire)
```

## Components and Interfaces

### 1. Domain Models

Domain models are immutable data classes independent of protobuf:

```kotlin
package net.onefivefour.notes.data.models

data class Note(
    val filePath: String,
    val title: String,
    val content: String,
    val updatedAt: Long // Unix timestamp in milliseconds
)

data class CreateNoteParams(
    val title: String,
    val content: String,
    val path: String
)

data class UpdateNoteParams(
    val filePath: String,
    val content: String
)
```

### 2. Repository Interface

```kotlin
package net.onefivefour.notes.data.repository

interface NotesRepository {
    suspend fun createNote(params: CreateNoteParams): Result<Note>
    suspend fun listNotes(path: String = ""): Result<List<Note>>
    suspend fun getNote(filePath: String): Result<Note>
    suspend fun updateNote(params: UpdateNoteParams): Result<Note>
    suspend fun deleteNote(filePath: String): Result<Unit>
}
```

### 3. Network Configuration

```kotlin
package net.onefivefour.notes.network.config

data class NetworkConfig(
    val baseUrl: String,
    val requestTimeoutMs: Long = 30_000,
    val connectTimeoutMs: Long = 10_000,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1_000
) {
    companion object {
        fun default(baseUrl: String) = NetworkConfig(baseUrl = baseUrl)
    }
}
```

### 4. Error Types

```kotlin
package net.onefivefour.notes.network.error

sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class ServerError(val code: Int, message: String) : NetworkException(message)
    class ClientError(val code: Int, message: String) : NetworkException(message)
    class TimeoutError(message: String) : NetworkException(message)
    class SerializationError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
}
```

### 5. ConnectRPC Client Interface

```kotlin
package net.onefivefour.notes.network.client

interface ConnectRpcClient {
    suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res>
}
```

### 6. Network Data Source

```kotlin
package net.onefivefour.notes.data.source.network

internal interface NetworkDataSource {
    suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse
    suspend fun listNotes(request: ListNotesRequest): ListNotesResponse
    suspend fun getNote(request: GetNoteRequest): GetNoteResponse
    suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse
    suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse
}
```

### 7. Cache Data Source

```kotlin
package net.onefivefour.notes.data.source.cache

internal interface CacheDataSource {
    suspend fun saveNote(note: Note)
    suspend fun saveNotes(notes: List<Note>)
    suspend fun getNote(filePath: String): Note?
    suspend fun listNotes(path: String): List<Note>
    suspend fun deleteNote(filePath: String)
    suspend fun clear()
}
```

### 8. Mappers

```kotlin
package net.onefivefour.notes.data.mapper

internal object NoteMapper {
    fun toDomain(proto: notes.v1.Note): Note {
        return Note(
            filePath = proto.file_path,
            title = proto.title,
            content = proto.content,
            updatedAt = proto.updated_at
        )
    }
    
    fun toDomain(proto: CreateNoteResponse): Note {
        return Note(
            filePath = proto.file_path,
            title = proto.title,
            content = proto.content,
            updatedAt = proto.updated_at
        )
    }
    
    fun toDomain(proto: GetNoteResponse): Note {
        return Note(
            filePath = proto.file_path,
            title = proto.title,
            content = proto.content,
            updatedAt = proto.updated_at
        )
    }
}
```

## Data Models

### Protobuf Generation with Wire

Wire will generate Kotlin classes from `proto/notes.proto`:

```kotlin
// Generated by Wire
package notes.v1

data class Note(
    val file_path: String,
    val title: String,
    val content: String,
    val updated_at: Long
)

data class CreateNoteRequest(
    val title: String,
    val content: String,
    val path: String
)

// ... other generated classes
```

### SQLDelight Schema

```sql
-- composeApp/src/commonMain/sqldelight/net/onefivefour/notes/cache/Notes.sq

CREATE TABLE Note (
    filePath TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    updatedAt INTEGER NOT NULL,
    cachedAt INTEGER NOT NULL
);

CREATE INDEX note_path_index ON Note(filePath);

insertOrReplace:
INSERT OR REPLACE INTO Note(filePath, title, content, updatedAt, cachedAt)
VALUES (?, ?, ?, ?, ?);

selectByFilePath:
SELECT * FROM Note WHERE filePath = ?;

selectByPathPrefix:
SELECT * FROM Note WHERE filePath LIKE ? || '%' ORDER BY updatedAt DESC;

selectAll:
SELECT * FROM Note ORDER BY updatedAt DESC;

deleteByFilePath:
DELETE FROM Note WHERE filePath = ?;

deleteAll:
DELETE FROM Note;
```

## Implementation Details

### ConnectRPC Protocol Implementation

ConnectRPC uses HTTP POST with specific headers:

```kotlin
package net.onefivefour.notes.network.client

internal class ConnectRpcClientImpl(
    private val httpClient: HttpClient,
    private val config: NetworkConfig
) : ConnectRpcClient {
    
    override suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res> = withRetry(config.maxRetries) {
        try {
            val requestBody = requestSerializer(request)
            
            val response: HttpResponse = httpClient.post("${config.baseUrl}$path") {
                contentType(ContentType("application", "proto"))
                header("Connect-Protocol-Version", "1")
                setBody(requestBody)
            }
            
            when (response.status.value) {
                in 200..299 -> {
                    val responseBody = response.readBytes()
                    Result.success(responseDeserializer(responseBody))
                }
                in 400..499 -> Result.failure(
                    NetworkException.ClientError(response.status.value, response.bodyAsText())
                )
                in 500..599 -> Result.failure(
                    NetworkException.ServerError(response.status.value, response.bodyAsText())
                )
                else -> Result.failure(
                    NetworkException.NetworkError("Unexpected status: ${response.status}")
                )
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    private fun handleException(e: Exception): NetworkException {
        return when (e) {
            is HttpRequestTimeoutException -> NetworkException.TimeoutError("Request timed out")
            is ConnectTimeoutException -> NetworkException.TimeoutError("Connection timed out")
            else -> NetworkException.NetworkError("Network error: ${e.message}", e)
        }
    }
}
```

### Retry Logic

```kotlin
package net.onefivefour.notes.network.client

internal suspend fun <T> withRetry(
    maxRetries: Int,
    delayMs: Long = 1000,
    block: suspend () -> Result<T>
): Result<T> {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        val result = block()
        
        if (result.isSuccess) {
            return result
        }
        
        val exception = result.exceptionOrNull()
        lastException = exception as? Exception
        
        // Only retry on network errors and 5xx server errors
        val shouldRetry = when (exception) {
            is NetworkException.NetworkError -> true
            is NetworkException.ServerError -> true
            is NetworkException.TimeoutError -> true
            else -> false
        }
        
        if (!shouldRetry || attempt == maxRetries - 1) {
            return result
        }
        
        // Exponential backoff
        delay(delayMs * (1 shl attempt))
    }
    
    return Result.failure(lastException ?: Exception("Unknown error"))
}
```

### Network Data Source Implementation

```kotlin
package net.onefivefour.notes.data.source.network

internal class NetworkDataSourceImpl(
    private val client: ConnectRpcClient
) : NetworkDataSource {
    
    override suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse {
        return client.call(
            path = "/notes.v1.NotesService/CreateNote",
            request = request,
            requestSerializer = { it.encode() },
            responseDeserializer = { CreateNoteResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
    
    override suspend fun listNotes(request: ListNotesRequest): ListNotesResponse {
        return client.call(
            path = "/notes.v1.NotesService/ListNotes",
            request = request,
            requestSerializer = { it.encode() },
            responseDeserializer = { ListNotesResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
    
    // Similar implementations for getNote, updateNote, deleteNote
}
```

### Cache Data Source Implementation

```kotlin
package net.onefivefour.notes.data.source.cache

internal class CacheDataSourceImpl(
    private val database: NotesDatabase
) : CacheDataSource {
    
    private val queries = database.notesQueries
    
    override suspend fun saveNote(note: Note) {
        queries.insertOrReplace(
            filePath = note.filePath,
            title = note.title,
            content = note.content,
            updatedAt = note.updatedAt,
            cachedAt = System.currentTimeMillis()
        )
    }
    
    override suspend fun saveNotes(notes: List<Note>) {
        notes.forEach { saveNote(it) }
    }
    
    override suspend fun getNote(filePath: String): Note? {
        return queries.selectByFilePath(filePath).executeAsOneOrNull()?.toDomain()
    }
    
    override suspend fun listNotes(path: String): List<Note> {
        return if (path.isEmpty()) {
            queries.selectAll().executeAsList().map { it.toDomain() }
        } else {
            queries.selectByPathPrefix(path).executeAsList().map { it.toDomain() }
        }
    }
    
    override suspend fun deleteNote(filePath: String) {
        queries.deleteByFilePath(filePath)
    }
    
    override suspend fun clear() {
        queries.deleteAll()
    }
}
```

### Repository Implementation

```kotlin
package net.onefivefour.notes.data.repository

class NotesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val cacheDataSource: CacheDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : NotesRepository {
    
    override suspend fun createNote(params: CreateNoteParams): Result<Note> = withContext(dispatcher) {
        try {
            val request = CreateNoteRequest(
                title = params.title,
                content = params.content,
                path = params.path
            )
            
            val response = networkDataSource.createNote(request)
            val note = NoteMapper.toDomain(response)
            
            // Cache the created note
            cacheDataSource.saveNote(note)
            
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun listNotes(path: String): Result<List<Note>> = withContext(dispatcher) {
        try {
            // Try network first
            val request = ListNotesRequest(path = path)
            val response = networkDataSource.listNotes(request)
            val notes = response.notes.map { NoteMapper.toDomain(it) }
            
            // Update cache
            cacheDataSource.saveNotes(notes)
            
            Result.success(notes)
        } catch (e: Exception) {
            // Fall back to cache on network error
            if (e is NetworkException) {
                val cachedNotes = cacheDataSource.listNotes(path)
                if (cachedNotes.isNotEmpty()) {
                    return@withContext Result.success(cachedNotes)
                }
            }
            Result.failure(e)
        }
    }
    
    override suspend fun getNote(filePath: String): Result<Note> = withContext(dispatcher) {
        try {
            val request = GetNoteRequest(file_path = filePath)
            val response = networkDataSource.getNote(request)
            val note = NoteMapper.toDomain(response)
            
            // Update cache
            cacheDataSource.saveNote(note)
            
            Result.success(note)
        } catch (e: Exception) {
            // Fall back to cache on network error
            if (e is NetworkException) {
                val cachedNote = cacheDataSource.getNote(filePath)
                if (cachedNote != null) {
                    return@withContext Result.success(cachedNote)
                }
            }
            Result.failure(e)
        }
    }
    
    override suspend fun updateNote(params: UpdateNoteParams): Result<Note> = withContext(dispatcher) {
        try {
            val request = UpdateNoteRequest(
                file_path = params.filePath,
                content = params.content
            )
            
            val response = networkDataSource.updateNote(request)
            
            // Get the full note to return (update response only has timestamp)
            val fullNote = getNote(params.filePath).getOrThrow()
            val updatedNote = fullNote.copy(
                content = params.content,
                updatedAt = response.updated_at
            )
            
            // Update cache
            cacheDataSource.saveNote(updatedNote)
            
            Result.success(updatedNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNote(filePath: String): Result<Unit> = withContext(dispatcher) {
        try {
            val request = DeleteNoteRequest(file_path = filePath)
            networkDataSource.deleteNote(request)
            
            // Remove from cache
            cacheDataSource.deleteNote(filePath)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Dependency Injection with Koin

The system uses Koin for dependency injection, which provides excellent KMP support and works across all target platforms.

#### Koin Module Definition

```kotlin
package net.onefivefour.notes.di

import io.ktor.client.*
import io.ktor.client.plugins.*
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule = module {
    // Network Configuration
    single {
        NetworkConfig.default(
            baseUrl = getProperty("backend.url", "http://localhost:8080")
        )
    }
    
    // HTTP Client
    single {
        HttpClient {
            install(HttpTimeout) {
                val config: NetworkConfig = get()
                requestTimeoutMillis = config.requestTimeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
            }
        }
    }
    
    // ConnectRPC Client
    single<ConnectRpcClient> {
        ConnectRpcClientImpl(
            httpClient = get(),
            config = get()
        )
    }
    
    // Network Data Source
    single<NetworkDataSource> {
        NetworkDataSourceImpl(client = get())
    }
}

val dataModule = module {
    // SQLDelight Database (platform-specific driver provided separately)
    single {
        NotesDatabase(driver = get())
    }
    
    // Cache Data Source
    single<CacheDataSource> {
        CacheDataSourceImpl(database = get())
    }
    
    // Repository
    single<NotesRepository> {
        NotesRepositoryImpl(
            networkDataSource = get(),
            cacheDataSource = get(),
            dispatcher = Dispatchers.Default
        )
    }
}

// Combine all modules
val appModules = listOf(networkModule, dataModule)
```

#### Platform-Specific Database Driver Modules

**Android:**
```kotlin
// androidMain
val androidDatabaseModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = NotesDatabase.Schema,
            context = get(),
            name = "notes.db"
        )
    }
}
```

**iOS:**
```kotlin
// iosMain
val iosDatabaseModule = module {
    single<SqlDriver> {
        NativeSqliteDriver(
            schema = NotesDatabase.Schema,
            name = "notes.db"
        )
    }
}
```

**JVM (Desktop):**
```kotlin
// jvmMain
val jvmDatabaseModule = module {
    single<SqlDriver> {
        JdbcSqliteDriver(
            url = "jdbc:sqlite:notes.db"
        ).also { NotesDatabase.Schema.create(it) }
    }
}
```

**JS/Wasm:**
```kotlin
// jsMain / wasmJsMain
val jsDatabaseModule = module {
    single<SqlDriver> {
        // Use in-memory or IndexedDB-backed driver
        JsDriver(NotesDatabase.Schema)
    }
}
```

#### Koin Initialization

**Common:**
```kotlin
// commonMain
expect fun platformModule(): Module

fun initKoin(appModule: Module = module { }) {
    startKoin {
        modules(appModule, platformModule(), appModules)
    }
}
```

**Android:**
```kotlin
// androidMain
actual fun platformModule() = module {
    single { androidContext() }
} + androidDatabaseModule
```

**iOS:**
```kotlin
// iosMain
actual fun platformModule() = iosDatabaseModule
```

#### Usage Example

```kotlin
class NotesViewModel : ViewModel() {
    private val repository: NotesRepository by inject()
    
    fun loadNotes() {
        viewModelScope.launch {
            repository.listNotes().fold(
                onSuccess = { notes -> /* update UI */ },
                onFailure = { error -> /* handle error */ }
            )
        }
    }
}
```

#### Testing with Koin

```kotlin
class NotesRepositoryTest : KoinTest {
    @Before
    fun setup() {
        startKoin {
            modules(module {
                single<NetworkDataSource> { MockNetworkDataSource() }
                single<CacheDataSource> { MockCacheDataSource() }
                single<NotesRepository> { NotesRepositoryImpl(get(), get()) }
            })
        }
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun testRepository() {
        val repository: NotesRepository by inject()
        // test repository
    }
}
```

### Factory Functions (Alternative to DI)

For cases where DI is not desired, factory functions are also provided:

```kotlin
package net.onefivefour.notes.data.repository

object NotesRepositoryFactory {
    fun create(
        config: NetworkConfig,
        database: NotesDatabase
    ): NotesRepository {
        val httpClient = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
            }
        }
        
        val connectRpcClient = ConnectRpcClientImpl(httpClient, config)
        val networkDataSource = NetworkDataSourceImpl(connectRpcClient)
        val cacheDataSource = CacheDataSourceImpl(database)
        
        return NotesRepositoryImpl(networkDataSource, cacheDataSource)
    }
}
```



## Correctness Properties

A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.

### Property 1: ConnectRPC Protocol Compliance

*For any* RPC request made through the Network_Client, the HTTP request SHALL include the header "Connect-Protocol-Version: 1" and use Content-Type "application/proto".

**Validates: Requirements 2.3**

### Property 2: Protobuf Serialization Round-Trip

*For any* valid protobuf message (CreateNoteRequest, UpdateNoteRequest, etc.), serializing then deserializing SHALL produce an equivalent message with all fields preserved.

**Validates: Requirements 2.4, 2.5**

### Property 3: Base URL Configuration

*For any* valid base URL configured in NetworkConfig, all RPC calls SHALL construct request URLs by concatenating the base URL with the service path.

**Validates: Requirements 2.6**

### Property 4: Domain-Proto Mapping Round-Trip

*For any* valid Note domain model, converting to proto then back to domain SHALL produce an equivalent Note with all fields preserved.

**Validates: Requirements 3.3, 3.4**

### Property 5: Timestamp Mapping Preservation

*For any* Unix timestamp in milliseconds, mapping from proto to domain and back SHALL preserve the exact timestamp value.

**Validates: Requirements 3.5**

### Property 6: Create-Then-Get Consistency

*For any* valid CreateNoteParams, after successfully creating a note, calling getNote with the returned filePath SHALL return a Note with matching title, content, and path.

**Validates: Requirements 4.2, 4.4**

### Property 7: Update-Then-Get Consistency

*For any* existing note and valid UpdateNoteParams, after successfully updating the note, calling getNote SHALL return a Note with the updated content and a newer updatedAt timestamp.

**Validates: Requirements 4.5**

### Property 8: Delete-Then-Get Consistency

*For any* existing note, after successfully deleting it, calling getNote with the same filePath SHALL fail with an error.

**Validates: Requirements 4.6**

### Property 9: Network Error Type Mapping

*For any* network request failure, the thrown exception type SHALL correctly match the failure mode: NetworkError for connection issues, ServerError for 5xx responses, ClientError for 4xx responses, and TimeoutError for timeouts.

**Validates: Requirements 6.2**

### Property 10: Error Message Preservation

*For any* server error response containing an error message, the thrown NetworkException SHALL contain that error message in its message field.

**Validates: Requirements 6.4**

### Property 11: Error Propagation Transparency

*For any* NetworkException thrown by the Network_Client, the Repository SHALL propagate the same exception type without wrapping or modifying it.

**Validates: Requirements 6.5**

### Property 12: Serialization Error Handling

*For any* invalid or corrupted data that cannot be deserialized, the System SHALL throw a SerializationError.

**Validates: Requirements 6.6**

### Property 13: Network-to-Cache Persistence

*For any* note successfully fetched via getNote or listNotes, the note SHALL be available in the cache immediately after the operation completes.

**Validates: Requirements 7.1**

### Property 14: Offline Cache Fallback

*For any* cached note, when network requests fail, calling getNote or listNotes SHALL return the cached data instead of failing.

**Validates: Requirements 7.2, 7.3**

### Property 15: Offline Operation Queueing

*For any* createNote or updateNote operation performed while offline, the operation SHALL be queued and not lost.

**Validates: Requirements 7.4**

### Property 16: Sync Queue Execution Order

*For any* sequence of queued offline operations, when network connectivity is restored, the operations SHALL be executed in the same order they were queued.

**Validates: Requirements 7.5**

### Property 17: Cache Persistence Across Restarts

*For any* note stored in cache, restarting the application SHALL preserve the cached note with all fields intact.

**Validates: Requirements 7.6**

### Property 18: Cache-First Response Pattern

*For any* cached note, calling getNote SHALL return the cached data immediately (not waiting for network), then update the cache in the background if network succeeds.

**Validates: Requirements 7.7**

### Property 19: Transient Error Retry Behavior

*For any* network request that fails with a transient error (NetworkError, ServerError, TimeoutError), the Network_Client SHALL retry the request at least once before failing.

**Validates: Requirements 8.1**

### Property 20: Retry Attempt Limit

*For any* network request that continuously fails, the Network_Client SHALL attempt at most 3 total requests (1 initial + 2 retries) before throwing the final error.

**Validates: Requirements 8.2**

### Property 21: Selective Retry by Error Type

*For any* network request failure, retries SHALL only occur for NetworkError, ServerError (5xx), and TimeoutError, but NOT for ClientError (4xx).

**Validates: Requirements 8.3, 8.4**

### Property 22: Final Error Preservation

*For any* network request that exhausts all retry attempts, the thrown exception SHALL be the error from the last retry attempt, not the first.

**Validates: Requirements 8.5**

### Property 23: Coroutine Cancellation Propagation

*For any* Repository operation that is cancelled via coroutine cancellation, the underlying network request SHALL also be cancelled and not complete.

**Validates: Requirements 10.3**

## Error Handling

### Error Type Hierarchy

All network errors inherit from `NetworkException`:

```kotlin
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

### Error Handling Strategy

1. **Network Layer**: Catches HTTP exceptions and converts to typed NetworkException
2. **Data Layer**: Propagates NetworkException, implements cache fallback
3. **Domain Layer**: Receives Result<T> and handles success/failure cases

### Retry Strategy

- Automatic retry for transient errors (network, server 5xx, timeout)
- No retry for client errors (4xx)
- Exponential backoff: 1s, 2s, 4s
- Maximum 3 attempts total

### Offline Handling

- Cache-first for reads when network fails
- Queue writes when offline
- Sync queue on reconnection
- Conflict resolution: last-write-wins

## Testing Strategy

### Dual Testing Approach

This feature requires both unit tests and property-based tests for comprehensive coverage:

- **Unit tests**: Verify specific examples, edge cases, and integration points
- **Property tests**: Verify universal properties across all inputs

### Property-Based Testing

We will use **Kotest Property Testing** for Kotlin Multiplatform, which supports all target platforms.

**Configuration**:
- Minimum 100 iterations per property test
- Each test tagged with: `Feature: notes-networking-data-layer, Property {N}: {property_text}`

**Property Test Coverage**:
- Protocol compliance (Property 1)
- Serialization round-trips (Properties 2, 4, 5)
- CRUD consistency (Properties 6, 7, 8)
- Error handling (Properties 9, 10, 11, 12)
- Caching behavior (Properties 13, 14, 15, 16, 17, 18)
- Retry logic (Properties 19, 20, 21, 22)
- Cancellation (Property 23)

### Unit Testing

**Focus Areas**:
- ConnectRPC client with mock HTTP responses
- Repository with mock network and cache data sources
- Mapper edge cases (empty strings, max values, special characters)
- Error scenarios (network timeout, server errors, malformed responses)
- Cache data source with in-memory SQLDelight database
- Factory functions create correct implementations

**Example Unit Tests**:
```kotlin
class NotesRepositoryTest {
    @Test
    fun `createNote with valid params returns success`() = runTest {
        val mockNetwork = MockNetworkDataSource()
        val mockCache = MockCacheDataSource()
        val repository = NotesRepositoryImpl(mockNetwork, mockCache)
        
        val result = repository.createNote(
            CreateNoteParams("Test", "Content", "/path")
        )
        
        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.title)
    }
    
    @Test
    fun `getNote falls back to cache when network fails`() = runTest {
        val mockNetwork = MockNetworkDataSource(shouldFail = true)
        val mockCache = MockCacheDataSource(
            cachedNotes = listOf(Note("path", "Title", "Content", 123L))
        )
        val repository = NotesRepositoryImpl(mockNetwork, mockCache)
        
        val result = repository.getNote("path")
        
        assertTrue(result.isSuccess)
        assertEquals("Title", result.getOrNull()?.title)
    }
}
```

### Integration Testing

- End-to-end tests with real HTTP server (MockWebServer)
- SQLDelight database tests with actual database instances
- Platform-specific tests for each KMP target

### Test Doubles

**FakeNotesRepository**:
```kotlin
class FakeNotesRepository : NotesRepository {
    private val notes = mutableMapOf<String, Note>()
    private var shouldFail = false
    val callLog = mutableListOf<String>()
    
    fun setShouldFail(fail: Boolean) {
        shouldFail = fail
    }
    
    override suspend fun createNote(params: CreateNoteParams): Result<Note> {
        callLog.add("createNote: ${params.title}")
        if (shouldFail) return Result.failure(NetworkException.NetworkError("Simulated failure"))
        
        val note = Note(params.path, params.title, params.content, System.currentTimeMillis())
        notes[note.filePath] = note
        return Result.success(note)
    }
    
    // ... other methods
}
```

**Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.5**
