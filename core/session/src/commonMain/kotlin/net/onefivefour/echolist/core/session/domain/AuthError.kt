package net.onefivefour.echolist.core.session.domain

sealed class AuthError {
    abstract val message: String

    data class InvalidCredentials(override val message: String) : AuthError()
    data class ServerError(override val message: String) : AuthError()
    data class NetworkError(override val message: String) : AuthError()
    data class Unknown(override val message: String) : AuthError()
}

class AuthFailure(val error: AuthError) : Exception(error.message)