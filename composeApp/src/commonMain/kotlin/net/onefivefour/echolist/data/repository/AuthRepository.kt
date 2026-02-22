package net.onefivefour.echolist.data.repository

/**
 * Repository abstraction for authentication operations.
 * Handles login, token refresh, and credential management via [SecureStorage].
 */
interface AuthRepository {
    suspend fun login(baseUrl: String, username: String, password: String): Result<Unit>
    suspend fun refreshToken(): Result<String>
    fun isAuthenticated(): Boolean
    fun clearAuth()
    fun getAccessToken(): String?
    fun getBaseUrl(): String?
}
