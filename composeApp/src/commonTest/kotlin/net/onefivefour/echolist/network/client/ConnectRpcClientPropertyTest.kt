package net.onefivefour.echolist.network.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import net.onefivefour.echolist.network.config.NetworkConfig
import net.onefivefour.echolist.network.error.NetworkException

/**
 * Property-based tests for ConnectRPC client.
 *
 * **Validates: Requirements 2.3, 2.4, 2.5, 2.6, 6.2, 6.4, 6.6**
 */
class ConnectRpcClientPropertyTest : FunSpec({

    // -- Generators --

    /** Generates realistic service paths like /notes.v1.SvcName/MethodName */
    val arbServicePath = arbitrary {
        val svc = Arb.string(3..10).bind().filter { it.isLetterOrDigit() }.take(6).ifEmpty { "Svc" }
        val method = Arb.string(3..10).bind().filter { it.isLetterOrDigit() }.take(6).ifEmpty { "Call" }
        "/notes.v1.$svc/$method"
    }

    /** Generates realistic base URLs with valid host/port. */
    val arbBaseUrl = arbitrary {
        val port = Arb.int(1000..9999).bind()
        "http://localhost:$port"
    }

    val arbErrorMessage = Arb.string(1..200)
    val arbServerErrorCode = Arb.int(500..599)
    val arbClientErrorCode = Arb.int(400..499)


    // -- Property 1: ConnectRPC Protocol Compliance --

    test("Property 1: All RPC requests include Connect-Protocol-Version header and application/proto content type") {
        checkAll(PropTestConfig(iterations = 20), arbServicePath) { path ->
            var capturedContentType: String? = null
            var capturedProtocolVersion: String? = null

            val mockEngine = MockEngine { request ->
                capturedContentType = request.body.contentType?.withoutParameters()?.toString()
                capturedProtocolVersion = request.headers["Connect-Protocol-Version"]
                respond(
                    content = byteArrayOf(),
                    status = HttpStatusCode.OK,
                    headers = headersOf()
                )
            }

            val client = ConnectRpcClientImpl(
                httpClient = HttpClient(mockEngine),
                config = NetworkConfig(baseUrl = "http://localhost:8080", maxRetries = 1)
            )

            client.call(
                path = path,
                request = byteArrayOf(1, 2, 3),
                requestSerializer = { it },
                responseDeserializer = { it }
            )

            capturedContentType shouldBe "application/proto"
            capturedProtocolVersion shouldBe "1"
        }
    }

    // -- Property 2: Protobuf Serialization Round-Trip --

    test("Property 2: Wire protobuf serialize then deserialize produces equivalent message") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.string(0..100),
            Arb.string(0..200),
            Arb.string(0..500),
            Arb.long(0L..Long.MAX_VALUE)
        ) { filePath, title, content, updatedAt ->
            val original = notes.v1.CreateNoteResponse(
                note = notes.v1.Note(
                    file_path = filePath,
                    title = title,
                    content = content,
                    updated_at = updatedAt
                )
            )
            val bytes = original.encode()
            val decoded = notes.v1.CreateNoteResponse.ADAPTER.decode(bytes)
            decoded shouldBe original
        }
    }

    // -- Property 3: Base URL Configuration --

    test("Property 3: All request URLs are constructed as baseUrl + servicePath") {
        checkAll(PropTestConfig(iterations = 20), arbBaseUrl, arbServicePath) { baseUrl, servicePath ->
            var capturedUrl: String? = null

            val mockEngine = MockEngine { request ->
                capturedUrl = request.url.toString()
                respond(
                    content = byteArrayOf(),
                    status = HttpStatusCode.OK,
                    headers = headersOf()
                )
            }

            val client = ConnectRpcClientImpl(
                httpClient = HttpClient(mockEngine),
                config = NetworkConfig(baseUrl = baseUrl, maxRetries = 1)
            )

            client.call(
                path = servicePath,
                request = byteArrayOf(),
                requestSerializer = { it },
                responseDeserializer = { it }
            )

            capturedUrl shouldBe "$baseUrl$servicePath"
        }
    }

    // -- Property 9: Network Error Type Mapping --

    test("Property 9: 5xx status codes produce ServerError") {
        checkAll(PropTestConfig(iterations = 20), arbServerErrorCode) { statusCode ->
            val mockEngine = MockEngine {
                respond(
                    content = "server error",
                    status = HttpStatusCode.fromValue(statusCode),
                    headers = headersOf()
                )
            }

            val client = ConnectRpcClientImpl(
                httpClient = HttpClient(mockEngine),
                config = NetworkConfig(baseUrl = "http://localhost:8080", maxRetries = 1)
            )

            val result = client.call(
                path = "/test/Method",
                request = byteArrayOf(),
                requestSerializer = { it },
                responseDeserializer = { it }
            )

            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
        }
    }

    test("Property 9: 4xx status codes produce ClientError") {
        checkAll(PropTestConfig(iterations = 20), arbClientErrorCode) { statusCode ->
            val mockEngine = MockEngine {
                respond(
                    content = "client error",
                    status = HttpStatusCode.fromValue(statusCode),
                    headers = headersOf()
                )
            }

            val client = ConnectRpcClientImpl(
                httpClient = HttpClient(mockEngine),
                config = NetworkConfig(baseUrl = "http://localhost:8080", maxRetries = 1)
            )

            val result = client.call(
                path = "/test/Method",
                request = byteArrayOf(),
                requestSerializer = { it },
                responseDeserializer = { it }
            )

            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
        }
    }

    // -- Property 10: Error Message Preservation --

    test("Property 10: Server error response message is preserved in NetworkException") {
        checkAll(PropTestConfig(iterations = 20), arbErrorMessage, arbServerErrorCode) { errorMsg, statusCode ->
            val mockEngine = MockEngine {
                respond(
                    content = errorMsg,
                    status = HttpStatusCode.fromValue(statusCode),
                    headers = headersOf()
                )
            }

            val client = ConnectRpcClientImpl(
                httpClient = HttpClient(mockEngine),
                config = NetworkConfig(baseUrl = "http://localhost:8080", maxRetries = 1)
            )

            val result = client.call(
                path = "/test/Method",
                request = byteArrayOf(),
                requestSerializer = { it },
                responseDeserializer = { it }
            )

            result.isFailure shouldBe true
            val exception = result.exceptionOrNull() as NetworkException.ServerError
            exception.message shouldContain errorMsg
        }
    }

    // -- Property 12: Serialization Error Handling --

    test("Property 12: Corrupted response data produces SerializationError") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(1..50)) { garbage ->
            val mockEngine = MockEngine {
                respond(
                    content = garbage.encodeToByteArray(),
                    status = HttpStatusCode.OK,
                    headers = headersOf()
                )
            }

            val client = ConnectRpcClientImpl(
                httpClient = HttpClient(mockEngine),
                config = NetworkConfig(baseUrl = "http://localhost:8080", maxRetries = 1)
            )

            val result = client.call(
                path = "/test/Method",
                request = byteArrayOf(),
                requestSerializer = { it },
                responseDeserializer = { bytes ->
                    throw IllegalArgumentException("Cannot decode: invalid protobuf")
                }
            )

            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NetworkException.SerializationError>()
        }
    }
})
