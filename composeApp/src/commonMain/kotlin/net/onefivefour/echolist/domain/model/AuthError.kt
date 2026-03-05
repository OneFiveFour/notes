package net.onefivefour.echolist.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.onefivefour.echolist.network.error.NetworkException

/**
 * Domain model representing authentication errors from the backend.
 * Maps ConnectRPC error codes to user-friendly error types.
 */
sealed class AuthError {
    abstract val message: String

    data class InvalidCredentials(override val message: String) : AuthError()
    data class ServerError(override val message: String) : AuthError()
    data class NetworkError(override val message: String) : AuthError()
    data class Unknown(override val message: String) : AuthError()

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parse ConnectRPC error response and create appropriate AuthError.
         * Expected JSON format: {"code": "unauthenticated" | "internal", "message": "error description"}
         */
        fun fromNetworkException(exception: Throwable): AuthError {
            val message = exception.message ?: "Unknown error occurred"
            
            // Try to parse JSON error response
            val errorResponse = parseErrorResponse(message)
            
            return when (errorResponse?.code) {
                "unauthenticated" -> InvalidCredentials(errorResponse.message)
                "internal" -> ServerError(errorResponse.message)
                else -> when (exception) {
                    is NetworkException.ClientError -> {
                        if (exception.code == 401) {
                            InvalidCredentials(message)
                        } else {
                            Unknown(message)
                        }
                    }
                    is NetworkException.ServerError -> {
                        ServerError(message)
                    }
                    is NetworkException.NetworkError,
                    is NetworkException.TimeoutError -> {
                        NetworkError(message)
                    }
                    else -> Unknown(message)
                }
            }
        }

        private fun parseErrorResponse(message: String): ErrorResponse? {
            return try {
                json.decodeFromString<ErrorResponse>(message)
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * ConnectRPC error response format.
 */
@Serializable
private data class ErrorResponse(
    val code: String,
    val message: String
)
