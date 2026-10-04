package net.onefivefour.echolist.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.source.JvmSecureStorage
import net.onefivefour.echolist.data.source.SecureStorage
import net.onefivefour.echolist.platform.echoListDatabasePath
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.sql.DriverManager

val databaseModule = module {
    single<SqlDriver> {
        createDesktopSqlDriver()
    }

    single {
        EchoListDatabase(driver = get())
    }

    single<SecureStorage> { JvmSecureStorage() }
}

private val expectedDesktopTables = setOf("Folder", "Note")

internal fun createDesktopSqlDriver(databasePath: Path = echoListDatabasePath()): SqlDriver {
    val absolutePath = databasePath.toAbsolutePath()
    absolutePath.parent?.let(Files::createDirectories)
    migrateLegacyWorkspaceDatabaseIfNeeded(absolutePath)

    val databaseAlreadyExists = Files.exists(absolutePath) && Files.size(absolutePath) > 0L
    val databaseUrl = desktopDatabaseUrl(absolutePath)

    return JdbcSqliteDriver(databaseUrl).also { driver ->
        initializeDesktopDatabase(
            driver = driver,
            databaseUrl = databaseUrl,
            databaseAlreadyExists = databaseAlreadyExists
        )
    }
}

internal fun desktopDatabaseUrl(databasePath: Path): String =
    "jdbc:sqlite:${databasePath.toAbsolutePath()}"

private fun migrateLegacyWorkspaceDatabaseIfNeeded(targetPath: Path) {
    val targetAlreadyPopulated = Files.exists(targetPath) && Files.size(targetPath) > 0L
    if (targetAlreadyPopulated) return

    val legacyDatabase = legacyDatabaseCandidates()
        .firstOrNull { candidate ->
            candidate != targetPath &&
                Files.exists(candidate) &&
                Files.size(candidate) > 0L
        } ?: return

    targetPath.parent?.let(Files::createDirectories)
    Files.copy(legacyDatabase, targetPath, REPLACE_EXISTING)
}

private fun legacyDatabaseCandidates(): Sequence<Path> = sequenceOf(
    Path.of("echolist.db").toAbsolutePath().normalize(),
    Path.of("composeApp", "echolist.db").toAbsolutePath().normalize()
).distinct()

private fun initializeDesktopDatabase(
    driver: SqlDriver,
    databaseUrl: String,
    databaseAlreadyExists: Boolean
) {
    val targetVersion = EchoListDatabase.Schema.version

    if (!databaseAlreadyExists) {
        createSchema(driver, databaseUrl, targetVersion)
        return
    }

    when (val currentVersion = readUserVersion(databaseUrl)) {
        0L -> adoptOrCreateUnversionedDatabase(driver, databaseUrl, targetVersion)
        targetVersion -> Unit
        in 1 until targetVersion -> {
            EchoListDatabase.Schema.migrate(driver, currentVersion, targetVersion)
            writeUserVersion(databaseUrl, targetVersion)
        }
        else -> error(
            "Desktop database version $currentVersion is newer than supported schema version $targetVersion."
        )
    }
}

private fun createSchema(driver: SqlDriver, databaseUrl: String, targetVersion: Long) {
    EchoListDatabase.Schema.create(driver)
    writeUserVersion(databaseUrl, targetVersion)
}

private fun adoptOrCreateUnversionedDatabase(
    driver: SqlDriver,
    databaseUrl: String,
    targetVersion: Long
) {
    when (countExpectedTables(databaseUrl)) {
        0 -> createSchema(driver, databaseUrl, targetVersion)
        expectedDesktopTables.size -> writeUserVersion(databaseUrl, targetVersion)
        else -> error("Desktop database is partially initialized and cannot be migrated automatically.")
    }
}

private fun countExpectedTables(databaseUrl: String): Int =
    DriverManager.getConnection(databaseUrl).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN ('Folder', 'Note')"
            ).use { resultSet ->
                if (resultSet.next()) resultSet.getInt(1) else 0
            }
        }
    }

private fun readUserVersion(databaseUrl: String): Long =
    DriverManager.getConnection(databaseUrl).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA user_version").use { resultSet ->
                if (resultSet.next()) resultSet.getLong(1) else 0L
            }
        }
    }

private fun writeUserVersion(databaseUrl: String, version: Long) {
    DriverManager.getConnection(databaseUrl).use { connection ->
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA user_version = $version")
        }
    }
}
