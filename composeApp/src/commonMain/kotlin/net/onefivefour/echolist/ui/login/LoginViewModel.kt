package net.onefivefour.echolist.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.error_backend_url_required
import echolist.composeapp.generated.resources.error_password_required
import echolist.composeapp.generated.resources.error_username_required
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.repository.AuthRepository
import net.onefivefour.echolist.data.source.SecureStorage
import net.onefivefour.echolist.data.source.StorageKeys
import net.onefivefour.echolist.domain.model.AuthError
import net.onefivefour.echolist.network.config.NetworkConfigProvider
import org.jetbrains.compose.resources.getString

class LoginViewModel(
    secureStorage: SecureStorage,
    private val authRepository: AuthRepository,
    private val networkConfigProvider: NetworkConfigProvider? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess: SharedFlow<Unit> = _loginSuccess.asSharedFlow()

    init {
        val storedUrl = secureStorage.get(StorageKeys.BACKEND_URL)
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
                    networkConfigProvider?.updateBaseUrl(current.backendUrl.trim())
                    _uiState.update { it.copy(isLoading = false) }
                    _loginSuccess.emit(Unit)
                },
                onFailure = { throwable ->
                    val authError = AuthError.fromNetworkException(throwable)
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