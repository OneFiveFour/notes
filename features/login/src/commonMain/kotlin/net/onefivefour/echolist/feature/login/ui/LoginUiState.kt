package net.onefivefour.echolist.feature.login.ui

import net.onefivefour.echolist.core.session.domain.AuthError

internal data class LoginUiState(
    val backendUrl: String = "https://",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val authError: AuthError? = null,
    val backendUrlError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null
)