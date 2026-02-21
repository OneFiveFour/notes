package net.onefivefour.notes.di

import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class KoinModuleVerificationTest : FunSpec({

    test("networkModule - all dependencies are satisfied") {
        networkModule.verify(
            extraTypes = listOf(
                // HttpClientEngine is provided internally by Ktor at runtime,
                // not through Koin. It's selected based on the platform.
                io.ktor.client.engine.HttpClientEngine::class,
                // These are provided by authModule at runtime
                net.onefivefour.notes.data.source.SecureStorage::class,
                net.onefivefour.notes.data.repository.AuthRepository::class,
                kotlinx.coroutines.flow.MutableSharedFlow::class
            )
        )
    }

    test("dataModule - all dependencies are satisfied") {
        dataModule.verify(
            extraTypes = listOf(
                // NotesDatabase is provided by the platform-specific databaseModule
                net.onefivefour.notes.cache.NotesDatabase::class
            )
        )
    }
})
