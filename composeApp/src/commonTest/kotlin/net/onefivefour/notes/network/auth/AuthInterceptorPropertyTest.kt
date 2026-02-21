package net.onefivefour.notes.network.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import net.onefivefour.notes.data.repository.AuthRepository
import net.onefivefour.notes.data.source.FakeSecureStorage
import net.onefivefour.notes.data.source.StorageKeys

/**
 * Property-based tests for AuthInterceptor Ktor plugin.
 */
class AuthInterceptorPropertyTest : FunSpec({

    // -- Generators --

    /** Generates non-auth paths that don't match login/refresh endpoints. */
    val arbNonAuthPath = arbitrary {
        val segment = Arb.string(3..15).bind().replace(Regex("[^a-zA-Z0-9]"), "a").ifEmpty { "api" }
        val method = Arb.string(3..10).bind().replace(Regex("[^a-zA-Z0-9]"), "b").ifEmpty { "call" }
        "/notes.v1.$segment/$method"
    }

    val arbToken = Arb.string(10..50)
    val arbPort = Arb.int(1000..9999)

    /**
     * Creates a fake [AuthRepository] backed by [FakeSecureStorage].
     * The [refreshResult] lambda controls what refreshToken() returns.
     */
    fun fakeAuthRepository(
        storage: FakeSecureStorage,
        refreshResult: suspend () -> Result<String> = { Result.failure(Exception("no refresh")) }
    ): AuthRepository = object : AuthRepository {
        override suspend fun login(baseUrl: String, username: String, password: String) =
            Result.success(Unit)

        override suspend fun refreshToken(): Result<String> = refreshResult()

        override fun isAuthenticated(): Boolean =
            storage.get(StorageKeys.ACCESS_TOKEN) != null

        override fun clearAuth() {
            storage.delete(StorageKeys.ACCESS_TOKEN)
            storage.delete(StorageKeys.REFRESH_TOKEN)
        }

        override fun getAccessToken(): String? =
            storage.get(StorageKeys.ACCESS_TOKEN)

        override fun getBaseUrl(): String? =
            storage.get(StorageKeys.BACKEND_URL)
    }

    // -----------------------------------------------------------------------
    // Property 10: Bearer token attachment on non-auth endpoints
    // -----------------------------------------------------------------------

    test("Property 10: Bearer token attached on non-auth endpoints when token exists") {
        checkAll(PropTestConfig(iterations = 20), arbNonAuthPath, arbToken, arbPort) { path, token, port ->
            val storage = FakeSecureStorage()
            storage.put(StorageKeys.ACCESS_TOKEN, token)

            var capturedAuthHeader: String? = null

            val mockEngine = MockEngine { request ->
                capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                respond(content = "ok", status = HttpStatusCode.OK)
            }

            val authEventFlow = MutableSharedFlow<AuthEvent>()
            val repo = fakeAuthRepository(storage)

            val client = HttpClient(mockEngine) {
                install(AuthInterceptor) {
                    this.authRepository = repo
                    this.authEventFlow = authEventFlow
                }
            }

            client.get("http://localhost:$port$path")

            capturedAuthHeader shouldBe "Bearer $token"
        }
    }

    // -----------------------------------------------------------------------
    // Property 8: 401 triggers refresh-store-retry
    // -----------------------------------------------------------------------

    test("Property 8: 401 triggers refresh, stores new token, and retries with new Bearer") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbNonAuthPath,
            arbToken,
            arbToken,
            arbPort
        ) { path, oldToken, newToken, port ->
            val storage = FakeSecureStorage()
            storage.put(StorageKeys.ACCESS_TOKEN, oldToken)
            storage.put(StorageKeys.REFRESH_TOKEN, "refresh-placeholder")
            storage.put(StorageKeys.BACKEND_URL, "http://localhost:$port")

            var requestCount = 0
            var retryAuthHeader: String? = null

            val mockEngine = MockEngine { request ->
                requestCount++
                if (requestCount == 1) {
                    // First request: return 401
                    respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
                } else {
                    // Retry: capture the new auth header
                    retryAuthHeader = request.headers[HttpHeaders.Authorization]
                    respond(content = "ok", status = HttpStatusCode.OK)
                }
            }

            val authEventFlow = MutableSharedFlow<AuthEvent>()
            val repo = fakeAuthRepository(storage) {
                // Simulate successful refresh
                storage.put(StorageKeys.ACCESS_TOKEN, newToken)
                Result.success(newToken)
            }

            val client = HttpClient(mockEngine) {
                install(AuthInterceptor) {
                    this.authRepository = repo
                    this.authEventFlow = authEventFlow
                }
            }

            client.get("http://localhost:$port$path")

            // Verify: refresh was called (new token stored)
            storage.get(StorageKeys.ACCESS_TOKEN) shouldBe newToken
            // Verify: retry happened with new token
            requestCount shouldBe 2
            retryAuthHeader shouldBe "Bearer $newToken"
        }
    }

    // -----------------------------------------------------------------------
    // Property 9: Refresh failure clears auth
    // -----------------------------------------------------------------------

    test("Property 9: Refresh failure clears tokens and emits ReAuthRequired") {
        checkAll(PropTestConfig(iterations = 20), arbNonAuthPath, arbToken, arbPort) { path, token, port ->
            runTest {
                val storage = FakeSecureStorage()
                storage.put(StorageKeys.ACCESS_TOKEN, token)
                storage.put(StorageKeys.REFRESH_TOKEN, "refresh-placeholder")

                val mockEngine = MockEngine {
                    // Always return 401
                    respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
                }

                val authEventFlow = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
                val repo = fakeAuthRepository(storage) {
                    Result.failure(Exception("refresh failed"))
                }

                val client = HttpClient(mockEngine) {
                    install(AuthInterceptor) {
                        this.authRepository = repo
                        this.authEventFlow = authEventFlow
                    }
                }

                // Collect the event in a separate coroutine
                var emittedEvent: AuthEvent? = null
                val collectJob = launch {
                    emittedEvent = authEventFlow.first()
                }

                client.get("http://localhost:$port$path")

                collectJob.join()

                // Verify: tokens cleared
                storage.get(StorageKeys.ACCESS_TOKEN) shouldBe null
                storage.get(StorageKeys.REFRESH_TOKEN) shouldBe null
                // Verify: ReAuthRequired emitted
                emittedEvent shouldNotBe null
                emittedEvent shouldBe AuthEvent.ReAuthRequired
            }
        }
    }
})
