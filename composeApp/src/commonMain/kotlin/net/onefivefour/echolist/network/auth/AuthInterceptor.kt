package net.onefivefour.echolist.network.auth

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.onefivefour.echolist.data.repository.AuthRepository

/**
 * Configuration for the [AuthInterceptor] Ktor client plugin.
 */
class AuthInterceptorConfig {
    lateinit var authRepository: AuthRepository
    lateinit var authEventFlow: MutableSharedFlow<AuthEvent>
}

private val PUBLIC_AUTH_ENDPOINTS = setOf(
    "/auth.v1.AuthService/Login",
    "/auth.v1.AuthService/RefreshToken"
)

private fun isAuthEndpoint(path: String): Boolean {
    return PUBLIC_AUTH_ENDPOINTS.any { path.endsWith(it) }
}

/**
 * Ktor client plugin that:
 * 1. Attaches Bearer token on non-auth endpoints from SecureStorage
 * 2. Handles 401 responses: acquires mutex, refreshes token, retries original request
 * 3. On refresh failure: clears auth and emits [AuthEvent.ReAuthRequired]
 */
val AuthInterceptor = createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
    val authRepository = pluginConfig.authRepository
    val authEventFlow = pluginConfig.authEventFlow
    val refreshMutex = Mutex()

    onRequest { request, _ ->
        val path = request.url.buildString()
        if (!isAuthEndpoint(path)) {
            authRepository.getAccessToken()?.let { token ->
                request.header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    on(Send) { request ->
        val originalCall = proceed(request)

        val path = originalCall.request.url.encodedPath
        if (originalCall.response.status == HttpStatusCode.Unauthorized && !isAuthEndpoint(path)) {
            val refreshResult = refreshMutex.withLock {
                authRepository.refreshToken()
            }

            if (refreshResult.isSuccess) {
                // Retry with the new token
                val newToken = refreshResult.getOrThrow()
                val retryRequest = HttpRequestBuilder().apply {
                    takeFrom(request)
                    headers.remove(HttpHeaders.Authorization)
                    header(HttpHeaders.Authorization, "Bearer $newToken")
                }
                proceed(retryRequest)
            } else {
                authRepository.clearAuth()
                authEventFlow.emit(AuthEvent.ReAuthRequired)
                originalCall
            }
        } else {
            originalCall
        }
    }
}
