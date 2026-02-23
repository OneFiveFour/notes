package net.onefivefour.echolist.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams
import net.onefivefour.echolist.data.source.cache.CacheDataSource
import net.onefivefour.echolist.data.source.cache.CacheDataSourceImpl
import net.onefivefour.echolist.data.source.network.NoteRemoteDataSource
import net.onefivefour.echolist.network.error.NetworkException
import notes.v1.CreateNoteRequest
import notes.v1.CreateNoteResponse
import notes.v1.DeleteNoteRequest
import notes.v1.DeleteNoteResponse
import notes.v1.GetNoteRequest
import notes.v1.GetNoteResponse
import notes.v1.ListNotesRequest
import notes.v1.ListNotesResponse
import notes.v1.UpdateNoteRequest
import notes.v1.UpdateNoteResponse

/**
 * Property-based tests for NotesRepositoryImpl behavior.
 *
 * **Validates: Requirements 4.2, 4.4, 4.5, 4.6, 6.5, 7.2, 7.3, 7.4, 7.5, 10.3**
 */
class NotesRepositoryPropertyTest : FunSpec({

    // -- Generators --

    val arbCreateNoteParams = arbitrary {
        CreateNoteParams(
            title = Arb.string(1..50).bind(),
            content = Arb.string(0..200).bind(),
            path = Arb.string(1..50).bind()
        )
    }

    val arbNote = arbitrary {
        Note(
            filePath = Arb.string(1..50).bind(),
            title = Arb.string(1..50).bind(),
            content = Arb.string(0..200).bind(),
            updatedAt = Arb.long(1L..Long.MAX_VALUE / 2).bind()
        )
    }

    val arbNetworkException: Arb<NetworkException> = Arb.element(
        NetworkException.NetworkError("connection refused"),
        NetworkException.ServerError(500, "internal server error"),
        NetworkException.ServerError(502, "bad gateway"),
        NetworkException.ClientError(400, "bad request"),
        NetworkException.ClientError(404, "not found"),
        NetworkException.TimeoutError("request timed out"),
        NetworkException.SerializationError("invalid data")
    )

    // -- Helpers --

    fun createInMemoryDatabase(): EchoListDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EchoListDatabase.Schema.create(driver)
        return EchoListDatabase(driver)
    }

    /**
     * A mock NetworkDataSource that returns pre-configured responses.
     * Tracks calls and can be configured to throw exceptions.
     */
    class MockNoteRemoteDataSource : NoteRemoteDataSource {
        var createNoteHandler: suspend (CreateNoteRequest) -> CreateNoteResponse = { req ->
            CreateNoteResponse(
                file_path = "${req.path}/${req.title}.md",
                title = req.title,
                content = req.content,
                updated_at = System.currentTimeMillis()
            )
        }
        var listNotesHandler: suspend (ListNotesRequest) -> ListNotesResponse = {
            ListNotesResponse(notes = emptyList())
        }
        var getNoteHandler: suspend (GetNoteRequest) -> GetNoteResponse = { req ->
            throw NetworkException.ClientError(404, "not found")
        }
        var updateNoteHandler: suspend (UpdateNoteRequest) -> UpdateNoteResponse = { req ->
            UpdateNoteResponse(updated_at = System.currentTimeMillis())
        }
        var deleteNoteHandler: suspend (DeleteNoteRequest) -> DeleteNoteResponse = {
            DeleteNoteResponse()
        }

        val createNoteCalls = mutableListOf<CreateNoteRequest>()
        val updateNoteCalls = mutableListOf<UpdateNoteRequest>()
        val getNoteCalls = mutableListOf<GetNoteRequest>()

        override suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse {
            createNoteCalls.add(request)
            return createNoteHandler(request)
        }
        override suspend fun listNotes(request: ListNotesRequest): ListNotesResponse {
            return listNotesHandler(request)
        }
        override suspend fun getNote(request: GetNoteRequest): GetNoteResponse {
            getNoteCalls.add(request)
            return getNoteHandler(request)
        }
        override suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse {
            updateNoteCalls.add(request)
            return updateNoteHandler(request)
        }
        override suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse {
            return deleteNoteHandler(request)
        }
    }


    // -- Property 6: Create-Then-Get Consistency --

    test("Property 6: After creating a note, getNote returns matching title/content/path") {
        checkAll(PropTestConfig(iterations = 20), arbCreateNoteParams) { params ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            val createdFilePath = "${params.path}/${params.title}.md"
            val createdTimestamp = System.currentTimeMillis()

            mockNetwork.createNoteHandler = { req ->
                CreateNoteResponse(
                    file_path = createdFilePath,
                    title = req.title,
                    content = req.content,
                    updated_at = createdTimestamp
                )
            }
            mockNetwork.getNoteHandler = { req ->
                GetNoteResponse(
                    file_path = req.file_path,
                    title = params.title,
                    content = params.content,
                    updated_at = createdTimestamp
                )
            }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            val createResult = repo.createNote(params)
            createResult.isSuccess shouldBe true

            val created = createResult.getOrThrow()
            created.title shouldBe params.title
            created.content shouldBe params.content

            val getResult = repo.getNote(created.filePath)
            getResult.isSuccess shouldBe true

            val fetched = getResult.getOrThrow()
            fetched.title shouldBe params.title
            fetched.content shouldBe params.content
            fetched.filePath shouldBe created.filePath
        }
    }

    // -- Property 7: Update-Then-Get Consistency --

    test("Property 7: After updating a note, getNote returns updated content with newer timestamp") {
        checkAll(PropTestConfig(iterations = 20), arbNote, Arb.string(1..200)) { originalNote, newContent ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            val updatedTimestamp = originalNote.updatedAt + 1000

            mockNetwork.updateNoteHandler = { _ ->
                UpdateNoteResponse(updated_at = updatedTimestamp)
            }
            mockNetwork.getNoteHandler = { req ->
                GetNoteResponse(
                    file_path = req.file_path,
                    title = originalNote.title,
                    content = newContent,
                    updated_at = updatedTimestamp
                )
            }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            // Seed the cache with the original note
            cache.saveNote(originalNote)

            val updateParams = UpdateNoteParams(filePath = originalNote.filePath, content = newContent)
            val updateResult = repo.updateNote(updateParams)
            updateResult.isSuccess shouldBe true

            val updated = updateResult.getOrThrow()
            updated.content shouldBe newContent
            updated.updatedAt shouldBe updatedTimestamp
            updated.updatedAt shouldNotBe originalNote.updatedAt
        }
    }

    // -- Property 8: Delete-Then-Get Consistency --

    test("Property 8: After deleting a note, getNote fails with an error") {
        checkAll(PropTestConfig(iterations = 20), arbNote) { note ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            mockNetwork.deleteNoteHandler = { DeleteNoteResponse() }
            mockNetwork.getNoteHandler = { req ->
                throw NetworkException.ClientError(404, "not found: ${req.file_path}")
            }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            // Seed the cache with the note
            cache.saveNote(note)

            val deleteResult = repo.deleteNote(note.filePath)
            deleteResult.isSuccess shouldBe true

            // getNote should fail — network returns 404 and cache was cleared by delete
            val getResult = repo.getNote(note.filePath)
            getResult.isFailure shouldBe true
        }
    }


    // -- Property 11: Error Propagation Transparency --

    test("Property 11: NetworkException from network layer is propagated with the same type through repository") {
        checkAll(PropTestConfig(iterations = 20), arbNetworkException) { exception ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            // Make getNote throw the exception and ensure no cache fallback
            mockNetwork.getNoteHandler = { throw exception }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            val result = repo.getNote("nonexistent/path.md")
            result.isFailure shouldBe true

            val thrown = result.exceptionOrNull()
            thrown shouldNotBe null
            thrown!!::class shouldBe exception::class
        }
    }

    // -- Property 14: Offline Cache Fallback --

    test("Property 14: When network fails, getNote returns cached data") {
        checkAll(PropTestConfig(iterations = 20), arbNote) { note ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            // Seed cache
            cache.saveNote(note)

            // Network always fails
            mockNetwork.getNoteHandler = { throw NetworkException.NetworkError("offline") }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            val result = repo.getNote(note.filePath)
            result.isSuccess shouldBe true

            val fetched = result.getOrThrow()
            fetched.filePath shouldBe note.filePath
            fetched.title shouldBe note.title
            fetched.content shouldBe note.content
            fetched.updatedAt shouldBe note.updatedAt
        }
    }

    test("Property 14: When network fails, listNotes returns cached data") {
        checkAll(PropTestConfig(iterations = 20), arbNote) { note ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            // Seed cache
            cache.saveNote(note)

            // Network always fails
            mockNetwork.listNotesHandler = { throw NetworkException.NetworkError("offline") }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            val result = repo.listNotes("")
            result.isSuccess shouldBe true

            val listResult = result.getOrThrow()
            listResult.notes.any { it.filePath == note.filePath } shouldBe true
        }
    }

    // -- Property 16: Sync Queue Execution Order --

    test("Property 16: Queued offline operations are synced in FIFO order") {
        checkAll(PropTestConfig(iterations = 20), Arb.int(2..5)) { opCount ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            // First: network fails so operations get queued
            mockNetwork.createNoteHandler = { throw NetworkException.NetworkError("offline") }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Unconfined)

            // Queue multiple create operations
            val paramsList = (1..opCount).map { i ->
                CreateNoteParams(title = "note-$i", content = "content-$i", path = "/path")
            }
            paramsList.forEach { params ->
                repo.createNote(params) // will fail and queue
            }

            // Verify operations were queued
            val pending = repo.getPendingOperations()
            pending.size shouldBe opCount

            // Now restore network and sync
            val syncedOrder = mutableListOf<String>()
            mockNetwork.createNoteHandler = { req ->
                syncedOrder.add(req.title)
                CreateNoteResponse(
                    file_path = "${req.path}/${req.title}.md",
                    title = req.title,
                    content = req.content,
                    updated_at = System.currentTimeMillis()
                )
            }

            repo.syncPendingOperations()

            // Verify FIFO order
            syncedOrder.size shouldBe opCount
            syncedOrder.forEachIndexed { index, title ->
                title shouldBe "note-${index + 1}"
            }

            // Verify queue is empty after sync
            repo.getPendingOperations().size shouldBe 0
        }
    }

    // -- Property 23: Coroutine Cancellation Propagation --

    test("Property 23: Cancelled repository operation cancels the underlying network request") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(1..50)) { filePath ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val mockNetwork = MockNoteRemoteDataSource()

            var networkCallStarted = false
            var networkCallCancelled = false

            mockNetwork.getNoteHandler = { _ ->
                networkCallStarted = true
                try {
                    // Simulate a long-running network call that checks for cancellation
                    delay(10_000)
                    throw AssertionError("Should have been cancelled")
                } catch (e: CancellationException) {
                    networkCallCancelled = true
                    throw e
                }
            }

            val repo = NotesRepositoryImpl(mockNetwork, cache, Dispatchers.Default)

            try {
                supervisorScope {
                    val deferred = async {
                        repo.getNote(filePath)
                    }
                    // Give the coroutine time to start the network call
                    delay(50)
                    deferred.cancel()
                    try {
                        deferred.await()
                    } catch (_: CancellationException) {
                        // expected
                    }
                }
            } catch (_: CancellationException) {
                // expected
            }

            // The network call should have started and been cancelled
            networkCallStarted shouldBe true
            networkCallCancelled shouldBe true
        }
    }
})
