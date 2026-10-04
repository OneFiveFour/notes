package net.onefivefour.echolist.core.database.di

import app.cash.sqldelight.driver.worker.WebWorkerDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.core.database.data.DatabaseProvider
import org.koin.dsl.module
import org.w3c.dom.Worker

private fun createDatabaseWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")

actual val databaseModule = module {
    single {
        DatabaseProvider {
            val driver = WebWorkerDriver(createDatabaseWorker())
            try {
                EchoListDatabase.Schema.create(driver).await()
                EchoListDatabase(driver)
            } catch (error: Exception) {
                driver.close()
                throw error
            }
        }
    }
}