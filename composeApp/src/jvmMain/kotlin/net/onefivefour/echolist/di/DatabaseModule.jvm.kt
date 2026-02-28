package net.onefivefour.echolist.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.source.JvmSecureStorage
import net.onefivefour.echolist.data.source.SecureStorage
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        JdbcSqliteDriver("jdbc:sqlite:echolist.db").also {
            // TODO: Schema.create() is called unconditionally on every app start,
            //  which recreates the database from scratch. Before the JVM target is
            //  used in production, replace this with proper migration handling
            //  (e.g. SqlDelight's migrateIfNeeded()) to preserve existing data.
            EchoListDatabase.Schema.create(it)
        }
    }

    single {
        EchoListDatabase(driver = get())
    }

    single<SecureStorage> { JvmSecureStorage() }
}
