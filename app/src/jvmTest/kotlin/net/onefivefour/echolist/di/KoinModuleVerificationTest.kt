package net.onefivefour.echolist.di

import net.onefivefour.echolist.core.database.di.databaseModule

import io.kotest.core.spec.style.FunSpec
import net.onefivefour.echolist.core.session.domain.AuthRepository
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class KoinModuleVerificationTest : FunSpec({

    test("dataModule - all dependencies are satisfied") {
        dataModule.verify(
            extraTypes = listOf(
                net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient::class,
                // EchoListDatabase is provided by the platform-specific databaseModule
                net.onefivefour.echolist.cache.EchoListDatabase::class
            )
        )
    }
})
