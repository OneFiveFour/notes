package net.onefivefour.echolist.ui.login

import net.onefivefour.echolist.domain.model.AuthError

data class LoginUiState(
    val backendUrl: String = "https://",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val authError: AuthError? = null,
    val backendUrlError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null
)