package net.onefivefour.notes.data.source.cache

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.notes.cache.NotesDatabase
import net.onefivefour.notes.data.models.Note

/**
 * Property-based tests for cache data source round-trip persistence.
 *
 * **Validates: Requirements 7.1, 7.6**
 */
class CacheDataSourcePropertyTest : FunSpec({

    // -- Generator --

    val arbNote = arbitrary {
        Note(
            filePath = Arb.string(1..100).bind(),
            title = Arb.string(0..200).bind(),
            content = Arb.string(0..500).bind(),
            updatedAt = Arb.long(0L..Long.MAX_VALUE / 2).bind()
        )
    }

    // -- Helpers --

    fun createInMemoryDatabase(): NotesDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NotesDatabase.Schema.create(driver)
        return NotesDatabase(driver)
    }

    // -- Property 13: Network-to-Cache Persistence --

    test("Property 13: For any note saved via saveNote, getNote with the same filePath returns an equivalent note") {
        checkAll(PropTestConfig(iterations = 100), arbNote) { note ->
            val db = createInMemoryDatabase()
            val cache: CacheDataSource = CacheDataSourceImpl(db)

            cache.saveNote(note)
            val retrieved = cache.getNote(note.filePath)

            retrieved shouldNotBe null
            retrieved!!.filePath shouldBe note.filePath
            retrieved.title shouldBe note.title
            retrieved.content shouldBe note.content
            retrieved.updatedAt shouldBe note.updatedAt
        }
    }

    // -- Property 17: Cache Persistence Across Restarts --

    test("Property 17: For any note stored in cache, re-querying after database re-open preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbNote) { note ->
            // Use a named in-memory database so it persists across driver instances
            // within the same JVM process via shared cache
            val jdbcUrl = "jdbc:sqlite:file:prop17_${note.filePath.hashCode()}?mode=memory&cache=shared"

            val driver1 = JdbcSqliteDriver(jdbcUrl)
            NotesDatabase.Schema.create(driver1)
            val db1 = NotesDatabase(driver1)
            val cache1: CacheDataSource = CacheDataSourceImpl(db1)

            cache1.saveNote(note)

            // "Restart": open a new driver/database instance against the same shared memory DB
            val driver2 = JdbcSqliteDriver(jdbcUrl)
            val db2 = NotesDatabase(driver2)
            val cache2: CacheDataSource = CacheDataSourceImpl(db2)

            val retrieved = cache2.getNote(note.filePath)

            retrieved shouldNotBe null
            retrieved!!.filePath shouldBe note.filePath
            retrieved.title shouldBe note.title
            retrieved.content shouldBe note.content
            retrieved.updatedAt shouldBe note.updatedAt

            // Clean up shared memory connections
            driver1.close()
            driver2.close()
        }
    }
})
