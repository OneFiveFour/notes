package net.onefivefour.echolist.core.session.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.session.domain.AuthError

class AuthErrorMapperTest : FunSpec({
    test("unauthenticated backend response keeps its user-facing message") {
        AuthErrorMapper.fromNetworkException(
            NetworkException.ClientError(401, """{"code":"unauthenticated","message":"Invalid password"}""")
        ) shouldBe AuthError.InvalidCredentials("Invalid password")
    }
    test("timeouts are mapped to domain network errors") {
        AuthErrorMapper.fromNetworkException(NetworkException.TimeoutError("Timed out")) shouldBe
            AuthError.NetworkError("Timed out")
    }
    test("server failures retain their message") {
        AuthErrorMapper.fromNetworkException(NetworkException.ServerError(500, "Unavailable")) shouldBe
            AuthError.ServerError("Unavailable")
    }
})