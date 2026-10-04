package net.onefivefour.echolist.core.database.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.cache.EchoListDatabase
import app.cash.sqldelight.async.coroutines.synchronous
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
        runTest {
            createDesktopSqlDriver(databasePath).use { driver ->
                EchoListDatabase(driver).notesQueries.insertOrReplace(
                    "note-1", "projects", "Desktop note", "Persistence matters", 1234L, 1234L
                )
            }
            createDesktopSqlDriver(databasePath).use { driver ->
                val saved = EchoListDatabase(driver).notesQueries.selectById("note-1").executeAsOne()
                saved.content shouldBe "Persistence matters"
                saved.title shouldBe "Desktop note"
                saved.parentDir shouldBe "projects"
                saved.updatedAt shouldBe 1234L
            }
        }

        readUserVersion(databasePath) shouldBe EchoListDatabase.Schema.version
    }

    test("legacy desktop databases without user_version are adopted in place") {
        val databasePath = Files.createTempDirectory("desktop-db").resolve("echolist.db")
        val databaseUrl = desktopDatabaseUrl(databasePath)

        JdbcSqliteDriver(databaseUrl).use { driver ->
            EchoListDatabase.Schema.synchronous().create(driver)
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
