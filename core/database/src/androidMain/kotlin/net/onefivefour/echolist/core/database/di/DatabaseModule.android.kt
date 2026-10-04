package net.onefivefour.echolist.core.database.di

import app.cash.sqldelight.async.coroutines.synchronous
import net.onefivefour.echolist.core.database.data.DatabaseProvider
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import org.koin.dsl.module

actual val databaseModule = module {
    single { DatabaseProvider { get<EchoListDatabase>() } }
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = EchoListDatabase.Schema.synchronous(),
            context = get(),
            name = "echolist.db"
        )
    }

    single {
        EchoListDatabase(driver = get())
    }


}
