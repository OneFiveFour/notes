package net.onefivefour.echolist.core.database.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import org.koin.dsl.module
import org.w3c.dom.Worker

private fun createDatabaseWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")

actual val databaseModule = module {
    single<SqlDriver> {
        WebWorkerDriver(createDatabaseWorker()).also { EchoListDatabase.Schema.create(it) }
    }

    single {
        EchoListDatabase(driver = get())
    }


}
