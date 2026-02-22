package net.onefivefour.echolist.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.source.IosSecureStorage
import net.onefivefour.echolist.data.source.SecureStorage
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        NativeSqliteDriver(
            schema = EchoListDatabase.Schema,
            name = "echolist.db"
        )
    }

    single {
        EchoListDatabase(driver = get())
    }

    single<SecureStorage> { IosSecureStorage() }
}
