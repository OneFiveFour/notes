package net.onefivefour.echolist.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.source.cache.CacheDataSource
import net.onefivefour.echolist.data.source.cache.CacheDataSourceImpl
import net.onefivefour.echolist.data.source.network.NetworkDataSource
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
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Property 18: Cache-First Response Pattern
 *
 * For any cached note, getNote returns cached data immediately without waiting for network.
 *
 * **Validates: Requirements 7.7**
 */
class CacheFirstPropertyTest : FunSpec({

    // -- Generator --

    val arbNote = arbitrary {
        Note(
            filePath = Arb.string(1..50).bind(),
            title = Arb.string(1..50).bind(),
            content = Arb.string(0..200).bind(),
            updatedAt = Arb.long(1L..Long.MAX_VALUE / 2).bind()
        )
    }

    // -- Helper --

    fun createInMemoryDatabase(): EchoListDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EchoListDatabase.Schema.create(driver)
        return EchoListDatabase(driver)
    }

    /**
     * A NetworkDataSource that introduces an artificial delay on getNote,
     * allowing us to verify the repository returns cached data before the
     * network call completes.
     */
    class DelayedNetworkDataSource(private val delayMs: Long) : NetworkDataSource {
        val networkCompleted = AtomicBoolean(false)

        override suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse {
            throw UnsupportedOperationException()
        }
        override suspend fun listNotes(request: ListNotesRequest): ListNotesResponse {
            throw UnsupportedOperationException()
        }
        override suspend fun getNote(request: GetNoteRequest): GetNoteResponse {
            delay(delayMs)
            networkCompleted.set(true)
            return GetNoteResponse(
                file_path = request.file_path,
                title = "network-title",
                content = "network-content",
                updated_at = System.currentTimeMillis()
            )
        }
        override suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse {
            throw UnsupportedOperationException()
        }
        override suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse {
            throw UnsupportedOperationException()
        }
    }

    // -- Property 18 --

    test("Property 18: For any cached note, getNote returns cached data immediately without waiting for network") {
        checkAll(PropTestConfig(iterations = 20), arbNote) { note ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)
            val delayedNetwork = DelayedNetworkDataSource(delayMs = 5_000)

            // Seed cache with the note
            cache.saveNote(note)

            val repo = NotesRepositoryImpl(delayedNetwork, cache, Dispatchers.Default)

            val result = repo.getNote(note.filePath)

            // The result should be the cached note, returned before the network completes
            result.isSuccess shouldBe true

            val fetched = result.getOrThrow()
            fetched.filePath shouldBe note.filePath
            fetched.title shouldBe note.title
            fetched.content shouldBe note.content
            fetched.updatedAt shouldBe note.updatedAt

            // The network call should NOT have completed yet — proving cache-first behavior
            delayedNetwork.networkCompleted.get() shouldBe false
        }
    }
})
