package net.onefivefour.echolist.core.session.di

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.SecureStorage
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class SessionModuleTest : FunSpec({
    test("session and authenticated transport resolve independently without a DI cycle") {
        val application = koinApplication {
            modules(sessionModule, module { single<SecureStorage> { FakeSecureStorage() } })
        }
        try {
            application.koin.get<AuthRepository>().isAuthenticated() shouldBe false
            val client = application.koin.get<ConnectRpcClient>()
            val publicClient = application.koin.get<ConnectRpcClient>(named("public-auth-client"))
            (client === publicClient) shouldBe false
        } finally {
            application.koin.get<HttpClient>().close()
            application.koin.get<HttpClient>(named("public-auth-http")).close()
            application.close()
        }
    }
})
