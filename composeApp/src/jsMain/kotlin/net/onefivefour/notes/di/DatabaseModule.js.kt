package net.onefivefour.notes.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import net.onefivefour.notes.cache.NotesDatabase
import net.onefivefour.notes.data.source.JsSecureStorage
import net.onefivefour.notes.data.source.SecureStorage
import org.koin.dsl.module
import org.w3c.dom.Worker

val databaseModule = module {
    single<SqlDriver> {
        WebWorkerDriver(
            Worker(
                js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
            )
        ).also { NotesDatabase.Schema.create(it) }
    }

    single {
        NotesDatabase(driver = get())
    }

    single<SecureStorage> { JsSecureStorage() }
}
