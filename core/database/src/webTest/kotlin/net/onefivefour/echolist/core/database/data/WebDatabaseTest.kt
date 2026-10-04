package net.onefivefour.echolist.core.database.data

import app.cash.sqldelight.async.coroutines.awaitAsOne
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.core.database.di.databaseModule
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class WebDatabaseTest {
    @Test
    fun workerInitializesSchemaAndExecutesQueries() = runTest {
        val application = koinApplication { modules(databaseModule) }
        try {
            val database = application.koin.get<DatabaseProvider>().get()
            database.notesQueries.insertOrReplace("web-note", "folder", "Web note", "Worker round trip", 1L, 2L)
            val saved = database.notesQueries.selectById("web-note").awaitAsOne()
            assertEquals("Worker round trip", saved.content)
            assertEquals("folder", saved.parentDir)
        } finally {
            application.close()
        }
    }
}