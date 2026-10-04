package net.onefivefour.echolist.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_backend_url_required
import net.onefivefour.echolist.feature.login.resources.error_password_required
import net.onefivefour.echolist.feature.login.resources.error_username_required
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.AuthError
import org.jetbrains.compose.resources.getString

internal class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess: SharedFlow<Unit> = _loginSuccess.asSharedFlow()

    init {
        val storedUrl = authRepository.getBaseUrl()
        if (storedUrl != null) {
            _uiState.update { it.copy(backendUrl = storedUrl) }
        }
    }

    fun onBackendUrlChanged(value: String) {
        _uiState.update { it.copy(backendUrl = value, backendUrlError = null, authError = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, authError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, authError = null) }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return

        _uiState.update { it.copy(isLoading = true, authError = null) }

        viewModelScope.launch {
            // Perform validation with localized error strings
            val backendUrlError = when {
                current.backendUrl.isBlank() -> getString(
                    Res.string.error_backend_url_required
                )
                else -> null
            }
            val usernameError = when {
                current.username.isBlank() -> getString(
                    Res.string.error_username_required
                )
                else -> null
            }
            val passwordError = when {
                current.password.isBlank() -> getString(
                    Res.string.error_password_required
                )
                else -> null
            }

            if (backendUrlError != null || usernameError != null || passwordError != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        backendUrlError = backendUrlError,
                        usernameError = usernameError,
                        passwordError = passwordError
                    )
                }
                return@launch
            }

            val result = authRepository.login(
                baseUrl = current.backendUrl.trim(),
                username = current.username.trim(),
                password = current.password.trim()
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _loginSuccess.emit(Unit)
                },
                onFailure = { throwable ->
                    val authError = (throwable as? net.onefivefour.echolist.core.session.domain.AuthFailure)?.error
                        ?: AuthError.Unknown(throwable.message ?: "Unknown error occurred")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authError = authError
                        )
                    }
                }
            )
        }
    }
}
