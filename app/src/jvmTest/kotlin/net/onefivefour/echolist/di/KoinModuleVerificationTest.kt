package net.onefivefour.echolist.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.notifications.domain.TaskCompletionHandler
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class KoinModuleVerificationTest : FunSpec({
    test("application composition resolves shared services without touching real storage or a backend") {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EchoListDatabase.Schema.create(driver)
        val application = koinApplication {
            modules(appModules)
            modules(module {
                single<SecureStorage> { FakeSecureStorage() }
                single { EchoListDatabase(driver) }
            })
        }
        try {
            val koin = application.koin
            koin.get<AuthRepository>().isAuthenticated() shouldBe false
            koin.get<TaskListRepository>().shouldBeInstanceOf<TaskListRepository>()
            koin.get<DirectoryChangeNotifier>().shouldBeInstanceOf<DirectoryChangeNotifier>()
            koin.get<TaskCompletionHandler>().shouldBeInstanceOf<TaskCompletionHandler>()
            koin.get<HttpClient>()
        } finally {
            application.close()
            driver.close()
        }
    }
})
