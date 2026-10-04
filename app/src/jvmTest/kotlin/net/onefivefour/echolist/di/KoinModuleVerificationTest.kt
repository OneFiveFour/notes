package net.onefivefour.echolist.di

import net.onefivefour.echolist.core.database.di.databaseModule

import io.kotest.core.spec.style.FunSpec
import net.onefivefour.echolist.core.session.domain.AuthRepository
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class KoinModuleVerificationTest : FunSpec({

    test("networkModule - all dependencies are satisfied") {
        networkModule.verify(
            extraTypes = listOf(
                net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient::class,
                // HttpClientEngine is provided internally by Ktor at runtime,
                // not through Koin. It's selected based on the platform.
                io.ktor.client.engine.HttpClientEngine::class,
                // These are provided by authModule at runtime
                net.onefivefour.echolist.core.session.domain.SecureStorage::class,
                AuthRepository::class,
                net.onefivefour.echolist.core.session.domain.AuthEventBus::class
            )
        )
    }

    test("dataModule - all dependencies are satisfied") {
        dataModule.verify(
            extraTypes = listOf(
                // EchoListDatabase is provided by the platform-specific databaseModule
                net.onefivefour.echolist.cache.EchoListDatabase::class
            )
        )
    }
})
