package net.onefivefour.notes.network.error

sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class ServerError(val code: Int, message: String) : NetworkException(message)
    class ClientError(val code: Int, message: String) : NetworkException(message)
    class TimeoutError(message: String) : NetworkException(message)
    class SerializationError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
}
