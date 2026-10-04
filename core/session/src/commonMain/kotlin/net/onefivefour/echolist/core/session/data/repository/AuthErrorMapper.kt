package net.onefivefour.echolist.core.session.data.repository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.onefivefour.echolist.core.session.domain.AuthError
import net.onefivefour.echolist.core.networking.data.error.NetworkException
internal object AuthErrorMapper {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parse ConnectRPC error response and create appropriate AuthError.
         * Expected JSON format: {"code": "unauthenticated" | "internal", "message": "error description"}
         */
        @Suppress("MagicNumber")
        fun fromNetworkException(exception: Throwable): AuthError {
            val message = exception.message ?: "Unknown error occurred"

            // Try to parse JSON error response
            val errorResponse = parseErrorResponse(message)

            return when (errorResponse?.code) {
                "unauthenticated" -> AuthError.InvalidCredentials(errorResponse.message)
                "internal" -> AuthError.ServerError(errorResponse.message)
                else -> when (exception) {
                    is NetworkException.ClientError -> {
                        if (exception.code == 401) {
                            AuthError.InvalidCredentials(message)
                        } else {
                            AuthError.Unknown(message)
                        }
                    }
                    is NetworkException.ServerError -> {
                        AuthError.ServerError(message)
                    }
                    is NetworkException.NetworkError,
                    is NetworkException.TimeoutError -> {
                        AuthError.NetworkError(message)
                    }
                    else -> AuthError.Unknown(message)
                }
            }
        }

        private fun parseErrorResponse(message: String): ErrorResponse? {
            return try {
                json.decodeFromString<ErrorResponse>(message)
            } catch (_: Exception) {
                null
            }
        }
    }
@Serializable
private data class ErrorResponse(
    val code: String,
    val message: String
)
