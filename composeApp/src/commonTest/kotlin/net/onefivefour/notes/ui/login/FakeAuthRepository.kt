package net.onefivefour.notes.ui.login

import net.onefivefour.notes.data.repository.AuthRepository

/**
 * Fake [AuthRepository] for LoginViewModel tests.
 * By default, login succeeds. Set [loginResult] to control behavior.
 */
open class FakeAuthRepository : AuthRepository {

    var loginResult: Result<Unit> = Result.success(Unit)

    private var authenticated = false

    open override suspend fun login(baseUrl: String, username: String, password: String): Result<Unit> {
        return loginResult.also { if (it.isSuccess) authenticated = true }
    }

    override suspend fun refreshToken(): Result<String> {
        return Result.failure(UnsupportedOperationException("Not used in login tests"))
    }

    override fun isAuthenticated(): Boolean = authenticated

    override fun clearAuth() {
        authenticated = false
    }

    override fun getAccessToken(): String? = if (authenticated) "fake_token" else null

    override fun getBaseUrl(): String? = null
}
