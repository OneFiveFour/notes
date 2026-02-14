# Implementation Plan: Notes Networking and Data Layer

## Overview

Implement a Kotlin Multiplatform networking and data layer for a notes application communicating with a Go backend via ConnectRPC over protobuf. The implementation follows clean architecture with network, cache, and domain layers, using Wire for protobuf codegen, Ktor for HTTP, SQLDelight for caching, and Koin for DI.

## Tasks

- [x] 1. Configure build dependencies and Wire protobuf code generation
  - [x] 1.1 Add Wire, Ktor, SQLDelight, Koin, kotlinx-coroutines, and Kotest dependencies to `gradle/libs.versions.toml` and `composeApp/build.gradle.kts`
    - Add Wire Gradle plugin and runtime dependency
    - Add Ktor client (core, content-negotiation, logging) for all KMP targets
    - Add SQLDelight plugin, runtime, and platform-specific drivers (Android, Native, JVM JDBC, JS)
    - Add Koin core and Koin KMP dependencies
    - Add kotlinx-coroutines-core and kotlinx-coroutines-test
    - Add Kotest framework, assertions, and property testing to commonTest
    - _Requirements: 1.1, 1.2, 2.2, 7.6, 9.3, 10.1, 11.1_
  - [x] 1.2 Configure Wire Gradle plugin to generate Kotlin from `proto/notes.proto`
    - Configure Wire source set pointing to `proto/` directory
    - Set output to a generated source directory excluded from version control
    - Ensure generated code targets `commonMain` and is compatible with all KMP targets
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Implement domain models and error types
  - [x] 2.1 Create domain data models in `commonMain`
    - Create `Note`, `CreateNoteParams`, `UpdateNoteParams` data classes in `net.onefivefour.notes.data.models`
    - All models must be immutable Kotlin data classes
    - _Requirements: 3.1, 3.2, 3.6_
  - [x] 2.2 Create the sealed error type hierarchy
    - Create `NetworkException` sealed class in `net.onefivefour.notes.network.error`
    - Include `NetworkError`, `ServerError`, `ClientError`, `TimeoutError`, `SerializationError` subtypes
    - _Requirements: 6.1, 6.3_
  - [x] 2.3 Create `NetworkConfig` data class
    - Create in `net.onefivefour.notes.network.config` with baseUrl, requestTimeoutMs, connectTimeoutMs, maxRetries, retryDelayMs
    - Provide `default()` factory with sensible defaults
    - Ensure immutability
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.6_

- [x] 3. Implement proto-to-domain mappers
  - [x] 3.1 Create `NoteMapper` object in `net.onefivefour.notes.data.mapper`
    - Implement `toDomain()` overloads for `notes.v1.Note`, `CreateNoteResponse`, `GetNoteResponse`
    - Implement `toProto()` functions for `CreateNoteParams` → `CreateNoteRequest`, `UpdateNoteParams` → `UpdateNoteRequest`
    - Handle timestamp mapping (Unix milliseconds preservation)
    - _Requirements: 3.3, 3.4, 3.5_
  - [x] 3.2 Write property tests for mapper round-trip
    - **Property 4: Domain-Proto Mapping Round-Trip**
    - **Property 5: Timestamp Mapping Preservation**
    - Generate arbitrary `Note` instances, convert to proto and back, assert equivalence
    - Generate arbitrary timestamps, map through proto round-trip, assert exact value preserved
    - Use Kotest property testing with minimum 100 iterations
    - **Validates: Requirements 3.3, 3.4, 3.5**

- [ ] 4. Implement ConnectRPC client and retry logic
  - [x] 4.1 Create `ConnectRpcClient` interface in `net.onefivefour.notes.network.client`
    - Define generic `call()` suspend function with path, request, serializer, and deserializer parameters
    - _Requirements: 2.1, 2.3, 2.7, 9.2_
  - [x] 4.2 Implement `ConnectRpcClientImpl` using Ktor
    - Use HTTP POST with `Content-Type: application/proto` and `Connect-Protocol-Version: 1` header
    - Map HTTP status codes to typed `NetworkException` subtypes (4xx → ClientError, 5xx → ServerError)
    - Handle timeout exceptions → `TimeoutError`, deserialization failures → `SerializationError`
    - Accept `NetworkConfig` for base URL and timeout configuration
    - _Requirements: 2.3, 2.4, 2.5, 2.6, 5.5, 6.2, 6.4, 6.6_
  - [x] 4.3 Implement `withRetry` utility function
    - Exponential backoff: delayMs * 2^attempt
    - Retry only on `NetworkError`, `ServerError`, `TimeoutError`; do NOT retry on `ClientError`
    - Maximum 3 total attempts (1 initial + 2 retries), configurable via `NetworkConfig`
    - On exhaustion, throw the last error
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_
  - [x] 4.4 Write property tests for ConnectRPC client and retry logic
    - **Property 1: ConnectRPC Protocol Compliance** — For any RPC request, verify Connect-Protocol-Version header and application/proto content type
    - **Property 2: Protobuf Serialization Round-Trip** — For any valid protobuf message, serialize then deserialize produces equivalent message
    - **Property 3: Base URL Configuration** — For any valid base URL, all request URLs are constructed as baseUrl + servicePath
    - **Property 9: Network Error Type Mapping** — For any failure mode, the thrown exception type matches (connection → NetworkError, 5xx → ServerError, 4xx → ClientError, timeout → TimeoutError)
    - **Property 10: Error Message Preservation** — For any server error response with a message, the NetworkException contains that message
    - **Property 12: Serialization Error Handling** — For any corrupted data, deserialization throws SerializationError
    - **Property 19: Transient Error Retry Behavior** — For any transient error, at least one retry occurs before failing
    - **Property 20: Retry Attempt Limit** — For any continuously failing request, at most 3 total attempts are made
    - **Property 21: Selective Retry by Error Type** — For any ClientError (4xx), no retry occurs; for NetworkError/ServerError/TimeoutError, retries occur
    - **Property 22: Final Error Preservation** — For any exhausted retry sequence, the thrown exception is from the last attempt
    - Use Kotest property testing with minimum 100 iterations per property
    - **Validates: Requirements 2.3, 2.4, 2.5, 2.6, 6.2, 6.4, 6.6, 8.1, 8.2, 8.3, 8.4, 8.5**

- [x] 5. Checkpoint — Verify core networking compiles and tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement network data source
  - [x] 6.1 Create `NetworkDataSource` interface in `net.onefivefour.notes.data.source.network`
    - Define suspend functions for all five RPC methods: createNote, listNotes, getNote, updateNote, deleteNote
    - Use Wire-generated request/response types as parameters and return types
    - _Requirements: 2.1_
  - [x] 6.2 Implement `NetworkDataSourceImpl`
    - Delegate each method to `ConnectRpcClient.call()` with correct service paths (`/notes.v1.NotesService/CreateNote`, etc.)
    - Use Wire ADAPTER for serialization/deserialization
    - Unwrap `Result` and throw on failure
    - _Requirements: 2.1, 2.4, 2.5_

- [x] 7. Implement SQLDelight cache data source
  - [x] 7.1 Create SQLDelight schema file at `composeApp/src/commonMain/sqldelight/net/onefivefour/notes/cache/Notes.sq`
    - Define `Note` table with filePath (PK), title, content, updatedAt, cachedAt columns
    - Define named queries: insertOrReplace, selectByFilePath, selectByPathPrefix, selectAll, deleteByFilePath, deleteAll
    - _Requirements: 7.1, 7.6_
  - [x] 7.2 Create `CacheDataSource` interface in `net.onefivefour.notes.data.source.cache`
    - Define suspend functions: saveNote, saveNotes, getNote, listNotes, deleteNote, clear
    - _Requirements: 7.1, 7.2, 7.3_
  - [x] 7.3 Implement `CacheDataSourceImpl` using SQLDelight generated queries
    - Map between domain `Note` and SQLDelight row types
    - Implement path-prefix filtering for listNotes
    - _Requirements: 7.1, 7.2, 7.3, 7.6_
  - [x] 7.4 Write property tests for cache round-trip
    - **Property 13: Network-to-Cache Persistence** — For any note saved via saveNote, getNote with the same filePath returns an equivalent note
    - **Property 17: Cache Persistence Across Restarts** — For any note stored in cache, re-querying after database re-open preserves all fields
    - Use in-memory SQLDelight driver for tests, Kotest property testing with minimum 100 iterations
    - **Validates: Requirements 7.1, 7.6**

- [x] 8. Implement repository with offline support
  - [x] 8.1 Create `NotesRepository` interface in `net.onefivefour.notes.data.repository`
    - Define suspend functions: createNote, listNotes, getNote, updateNote, deleteNote
    - All return `Result<T>` types
    - Accept `CoroutineDispatcher` parameter for structured concurrency
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.9, 9.1, 10.1, 10.5_
  - [x] 8.2 Implement `NotesRepositoryImpl`
    - Accept `NetworkDataSource`, `CacheDataSource`, and `CoroutineDispatcher` as constructor parameters
    - Use `withContext(dispatcher)` for all operations
    - Use `NoteMapper` for proto-to-domain conversion
    - Cache notes after successful network fetches
    - For read operations (listNotes, getNote): try network first, fall back to cache on `NetworkException`
    - For write operations (createNote, updateNote): queue when offline (store in pending operations)
    - Propagate `NetworkException` without catching/wrapping
    - _Requirements: 4.7, 4.8, 4.9, 6.5, 7.1, 7.2, 7.3, 7.4, 7.7, 10.4_
  - [x] 8.3 Implement offline operation queue and sync mechanism
    - Queue createNote/updateNote operations when network is unavailable
    - Sync queued operations in FIFO order when connectivity is restored
    - _Requirements: 7.4, 7.5_
  - [x] 8.4 Write property tests for repository behavior
    - **Property 6: Create-Then-Get Consistency** — For any valid CreateNoteParams, after creating a note, getNote returns matching title/content/path
    - **Property 7: Update-Then-Get Consistency** — For any existing note and valid update, getNote returns updated content with newer timestamp
    - **Property 8: Delete-Then-Get Consistency** — For any existing note, after deleting, getNote fails with an error
    - **Property 11: Error Propagation Transparency** — For any NetworkException from Network_Client, Repository propagates the same exception type
    - **Property 14: Offline Cache Fallback** — For any cached note, when network fails, getNote/listNotes returns cached data
    - **Property 16: Sync Queue Execution Order** — For any sequence of queued offline operations, sync executes them in FIFO order
    - **Property 23: Coroutine Cancellation Propagation** — For any cancelled repository operation, the underlying network request is also cancelled
    - Use mock NetworkDataSource and in-memory CacheDataSource, Kotest property testing with minimum 100 iterations
    - **Validates: Requirements 4.2, 4.4, 4.5, 4.6, 6.5, 7.2, 7.3, 7.4, 7.5, 10.3**

- [x] 9. Checkpoint — Verify repository and cache integration
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Implement dependency injection with Koin
  - [x] 10.1 Create Koin modules in `net.onefivefour.notes.di`
    - Define `networkModule`: NetworkConfig, HttpClient with timeout config, ConnectRpcClient, NetworkDataSource
    - Define `dataModule`: NotesDatabase, CacheDataSource, NotesRepository
    - Expose combined `appModules` list
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  - [x] 10.2 Create platform-specific SQLDelight driver modules
    - `androidMain`: AndroidSqliteDriver
    - `iosMain`: NativeSqliteDriver
    - `jvmMain`: JdbcSqliteDriver
    - `jsMain`/`wasmJsMain`: JS-compatible driver
    - _Requirements: 7.6_
  - [x] 10.3 Create `expect`/`actual` platform module and Koin initialization function
    - `expect fun platformModule(): Module` in commonMain
    - `actual` implementations in each platform source set
    - `initKoin()` function combining app modules and platform module
    - _Requirements: 9.3, 9.5_
  - [x] 10.4 Wire `initKoin()` into platform entry points
    - Android: Create `NotesApplication` extending `Application`, call `initKoin()` with `androidContext(this)`, register in `AndroidManifest.xml`
    - iOS: Call `initKoin()` before returning the `ComposeUIViewController` in `MainViewController.kt`
    - JVM: Call `initKoin()` at the start of `main()` in `main.kt`
    - Web (JS/WasmJs): Call `initKoin()` at the start of `main()` in `webMain/main.kt`
    - _Requirements: 9.3, 9.5_

- [ ] 11. Implement factory functions and test doubles
  - [ ] 11.1 Create `NotesRepositoryFactory` in `net.onefivefour.notes.data.repository`
    - Factory function accepting `NetworkConfig` and `NotesDatabase`, wiring all internal dependencies
    - _Requirements: 9.3_
  - [ ] 11.2 Create `FakeNotesRepository` for testing
    - Implement `NotesRepository` interface with in-memory storage
    - Support pre-configuring responses and simulating errors via `setShouldFail()`
    - Track all method calls in a `callLog` list for verification
    - Work synchronously without network calls
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_
  - [ ]* 11.3 Write unit tests for FakeNotesRepository
    - Verify pre-configured responses are returned correctly
    - Verify error simulation works
    - Verify call tracking records all invocations
    - _Requirements: 11.2, 11.3, 11.4_

- [ ] 12. Implement cache-first response pattern
  - [ ] 12.1 Update `NotesRepositoryImpl` to return cached data immediately and refresh in background
    - For getNote and listNotes: return cached data first if available, then launch background coroutine to fetch from network and update cache
    - Use structured concurrency (coroutineScope) for background refresh
    - Handle cancellation properly — cancel background refresh if parent scope is cancelled
    - _Requirements: 7.7, 10.3, 10.4_
  - [ ]* 12.2 Write property test for cache-first pattern
    - **Property 18: Cache-First Response Pattern** — For any cached note, getNote returns cached data immediately without waiting for network
    - Use mock NetworkDataSource with artificial delay, verify cached data returned before network completes
    - **Validates: Requirements 7.7**

- [ ] 13. Final checkpoint — Full integration verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests use Kotest property testing with minimum 100 iterations each
- Checkpoints ensure incremental validation at key integration points
- Wire-generated code from task 1.2 is a prerequisite for tasks 3.1 and 6.2
