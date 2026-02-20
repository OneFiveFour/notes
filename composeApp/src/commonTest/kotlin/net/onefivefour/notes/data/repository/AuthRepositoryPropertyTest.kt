package net.onefivefour.notes.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.notes.data.source.FakeSecureStorage
import net.onefivefour.notes.data.source.StorageKeys
import net.onefivefour.notes.network.client.ConnectRpcClient

/**
 * Property-based tests for AuthRepository login behavior.
 */
class AuthRepositoryPropertyTest : FunSpec({

    /**
     * Property 5: Successful login persists all credentials
     *
     * *For any* successful LoginResponse containing an access_token, refresh_token,
     * and the provided backend URL, after login completes, SecureStorage should
     * contain all three values.
     *
     * **Validates: Requirements 2.2, 2.3, 7.1**
     */
    test("Property 5: Successful login persists access_token, refresh_token, and backend URL") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..50),  // baseUrl
            Arb.string(1..50),  // username
            Arb.string(1..50),  // password
            Arb.string(1..100), // accessToken
            Arb.string(1..100)  // refreshToken
        ) { baseUrl, username, password, accessToken, refreshToken ->
            val storage = FakeSecureStorage()

            val mockClient = object : ConnectRpcClient {
                override suspend fun <Req, Res> call(
                    path: String,
                    request: Req,
                    requestSerializer: (Req) -> ByteArray,
                    responseDeserializer: (ByteArray) -> Res
                ): Result<Res> {
                    val response = auth.v1.LoginResponse(
                        access_token = accessToken,
                        refresh_token = refreshToken
                    )
                    val bytes = auth.v1.LoginResponse.ADAPTER.encode(response)
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(responseDeserializer(bytes) as Res)
                }
            }

            val repo = AuthRepositoryImpl(
                secureStorage = storage,
                clientFactory = { _ -> mockClient }
            )

            val result = repo.login(baseUrl, username, password)

            result.isSuccess shouldBe true
            storage.get(StorageKeys.ACCESS_TOKEN) shouldBe accessToken
            storage.get(StorageKeys.REFRESH_TOKEN) shouldBe refreshToken
            storage.get(StorageKeys.BACKEND_URL) shouldBe baseUrl
        }
    }

    /**
     * Property 4: Login request construction
     *
     * *For any* username and password string pair, when AuthRepository.login is called,
     * the system should send a LoginRequest protobuf message to the
     * /auth.v1.AuthService/Login path where the request's username and password
     * fields match the provided values.
     *
     * **Validates: Requirements 2.1**
     */
    test("Property 4: Login request path and protobuf fields match provided credentials") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..50),  // baseUrl
            Arb.string(1..50),  // username
            Arb.string(1..50)   // password
        ) { baseUrl, username, password ->
            val storage = FakeSecureStorage()
            var capturedPath: String? = null
            var capturedRequest: auth.v1.LoginRequest? = null

            val mockClient = object : ConnectRpcClient {
                override suspend fun <Req, Res> call(
                    path: String,
                    request: Req,
                    requestSerializer: (Req) -> ByteArray,
                    responseDeserializer: (ByteArray) -> Res
                ): Result<Res> {
                    capturedPath = path
                    // Decode the serialized request to verify protobuf fields
                    val serialized = requestSerializer(request)
                    capturedRequest = auth.v1.LoginRequest.ADAPTER.decode(serialized)

                    val response = auth.v1.LoginResponse(
                        access_token = "token",
                        refresh_token = "refresh"
                    )
                    val bytes = auth.v1.LoginResponse.ADAPTER.encode(response)
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(responseDeserializer(bytes) as Res)
                }
            }

            val repo = AuthRepositoryImpl(
                secureStorage = storage,
                clientFactory = { _ -> mockClient }
            )

            repo.login(baseUrl, username, password)

            capturedPath shouldBe "/auth.v1.AuthService/Login"
            capturedRequest?.username shouldBe username
            capturedRequest?.password shouldBe password
        }
    }
})
