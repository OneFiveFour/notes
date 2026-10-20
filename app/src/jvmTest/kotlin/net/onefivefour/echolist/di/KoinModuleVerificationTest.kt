package net.onefivefour.echolist.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import net.onefivefour.echolist.cache.EchoListDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.notifications.domain.TaskCompletionHandler
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.ReminderStore
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler

class KoinModuleVerificationTest : FunSpec({
    test("application composition resolves shared services without touching real storage or a backend") {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EchoListDatabase.Schema.synchronous().create(driver)
        val application = koinApplication {
            modules(appModules)
            modules(
                module {
                    single<SecureStorage> { FakeSecureStorage() }
                    single { EchoListDatabase(driver) }
                    single {
                        NotificationCoordinator(
                            object : ReminderStore {
                                var state = ReminderState()
                                override suspend fun read() = state
                                override suspend fun write(state: ReminderState) {
                                    this.state = state
                                }
                            },
                            object : ReminderPlatform {
                                override suspend fun schedule(reminder: Reminder) = Unit
                                override suspend fun cancelPending(reminder: Reminder) = Unit
                                override suspend fun dismiss(reminder: Reminder) = Unit
                                override suspend fun show(reminder: Reminder) = false
                            },
                            object : NotificationPermissionChecker {
                                override suspend fun isGranted() = false
                            },
                            get<NotificationAccountProvider>(),
                            object : ReminderBackgroundScheduler {
                                override fun start() = Unit
                                override fun requestSync() = Unit
                                override fun stop() = Unit
                            }
                        )
                    }
                }
            )
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