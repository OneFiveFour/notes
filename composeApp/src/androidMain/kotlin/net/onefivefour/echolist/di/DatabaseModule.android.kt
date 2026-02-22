package net.onefivefour.echolist.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.source.AndroidSecureStorage
import net.onefivefour.echolist.data.source.SecureStorage
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = EchoListDatabase.Schema,
            context = get(),
            name = "echolist.db"
        )
    }

    single {
        EchoListDatabase(driver = get())
    }

    single<SecureStorage> { AndroidSecureStorage(context = get()) }
}
