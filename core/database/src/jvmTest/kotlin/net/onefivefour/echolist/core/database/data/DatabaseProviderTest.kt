package net.onefivefour.echolist.core.database.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.cache.EchoListDatabase

class DatabaseProviderTest : FunSpec({
    test("concurrent callers await one completed initialization") {
        runTest {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
                var initializations = 0
                val database = EchoListDatabase(driver)
                val provider = DatabaseProvider {
                    initializations++
                    delay(10)
                    database
                }
                val databases = List(10) { async { provider.get() } }.awaitAll()
                initializations shouldBe 1
                databases.all { it === database } shouldBe true
            }
        }
    }

    test("failed initialization does not publish a partially initialized database") {
        runTest {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
                var attempts = 0
                val database = EchoListDatabase(driver)
                val provider = DatabaseProvider {
                    attempts++
                    if (attempts == 1) error("worker failed")
                    database
                }
                runCatching { provider.get() }.isFailure shouldBe true
                (provider.get() === database) shouldBe true
                attempts shouldBe 2
            }
        }
    }
})
