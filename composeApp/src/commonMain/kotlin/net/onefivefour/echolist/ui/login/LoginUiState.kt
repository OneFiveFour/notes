package net.onefivefour.echolist.ui.login

data class LoginUiState(
    val backendUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val backendUrlError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null
)
