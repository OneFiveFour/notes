package net.onefivefour.notes.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.onefivefour.notes.cache.NotesDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = NotesDatabase.Schema,
            context = get(),
            name = "notes.db"
        )
    }

    single {
        NotesDatabase(driver = get())
    }
}
