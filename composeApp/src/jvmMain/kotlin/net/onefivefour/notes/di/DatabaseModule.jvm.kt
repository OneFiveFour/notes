package net.onefivefour.notes.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.onefivefour.notes.cache.NotesDatabase
import net.onefivefour.notes.data.source.JvmSecureStorage
import net.onefivefour.notes.data.source.SecureStorage
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        JdbcSqliteDriver("jdbc:sqlite:notes.db").also {
            NotesDatabase.Schema.create(it)
        }
    }

    single {
        NotesDatabase(driver = get())
    }

    single<SecureStorage> { JvmSecureStorage() }
}
