package net.onefivefour.echolist.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.source.cache.CacheDataSourceImpl
import net.onefivefour.echolist.domain.model.Note
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager

class DesktopDatabaseDriverTest : FunSpec({

    test("new desktop databases are created with the current schema version") {
        val databasePath = Files.createTempDirectory("desktop-db").resolve("echolist.db")

        createDesktopSqlDriver(databasePath).close()

        readUserVersion(databasePath) shouldBe EchoListDatabase.Schema.version
    }

    test("existing desktop databases reopen without recreating the schema") {
        val databasePath = Files.createTempDirectory("desktop-db").resolve("echolist.db")
        val note = Note(
            id = "note-1",
            filePath = "/projects/demo.md",
            title = "Desktop note",
            content = "Persistence matters",
            updatedAt = 1234L
        )

        runTest {
            createDesktopSqlDriver(databasePath).use { driver ->
                val cache = CacheDataSourceImpl(EchoListDatabase(driver))
                cache.saveNote(note)
            }

            createDesktopSqlDriver(databasePath).use { driver ->
                val cache = CacheDataSourceImpl(EchoListDatabase(driver))
                cache.getNote(note.id) shouldBe note
            }
        }

        readUserVersion(databasePath) shouldBe EchoListDatabase.Schema.version
    }

    test("legacy desktop databases without user_version are adopted in place") {
        val databasePath = Files.createTempDirectory("desktop-db").resolve("echolist.db")
        val databaseUrl = desktopDatabaseUrl(databasePath)

        JdbcSqliteDriver(databaseUrl).use { driver ->
            EchoListDatabase.Schema.create(driver)
        }

        readUserVersion(databasePath) shouldBe 0L

        createDesktopSqlDriver(databasePath).close()

        readUserVersion(databasePath) shouldBe EchoListDatabase.Schema.version
    }
})

private fun readUserVersion(databasePath: Path): Long {
    DriverManager.getConnection(desktopDatabaseUrl(databasePath)).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA user_version").use { resultSet ->
                return if (resultSet.next()) resultSet.getLong(1) else 0L
            }
        }
    }
}
