package net.onefivefour.notes.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.onefivefour.notes.cache.NotesDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        NativeSqliteDriver(
            schema = NotesDatabase.Schema,
            name = "notes.db"
        )
    }

    single {
        NotesDatabase(driver = get())
    }
}
