package net.onefivefour.echolist.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.source.SecureStorage
import net.onefivefour.echolist.data.source.WasmJsSecureStorage
import org.koin.dsl.module
import org.w3c.dom.Worker

val databaseModule = module {
    single<SqlDriver> {
        WebWorkerDriver(
            Worker(
                js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
            )
        ).also { EchoListDatabase.Schema.create(it) }
    }

    single {
        EchoListDatabase(driver = get())
    }

    single<SecureStorage> { WasmJsSecureStorage() }
}
