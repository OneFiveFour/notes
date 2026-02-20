package net.onefivefour.notes.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.notes.data.repository.AuthRepository
import net.onefivefour.notes.data.source.SecureStorage
import net.onefivefour.notes.data.source.StorageKeys

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val storedUrl = secureStorage.get(StorageKeys.BACKEND_URL)
        if (storedUrl != null) {
            _uiState.update { it.copy(backendUrl = storedUrl) }
        }
    }

    fun onBackendUrlChanged(value: String) {
        _uiState.update { it.copy(backendUrl = value, backendUrlError = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return

        val backendUrlError = if (current.backendUrl.isBlank()) "Backend URL is required" else null
        val usernameError = if (current.username.isBlank()) "Username is required" else null
        val passwordError = if (current.password.isBlank()) "Password is required" else null

        if (backendUrlError != null || usernameError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    backendUrlError = backendUrlError,
                    usernameError = usernameError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.login(
                baseUrl = current.backendUrl.trim(),
                username = current.username.trim(),
                password = current.password.trim()
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Login failed"
                        )
                    }
                }
            )
        }
    }
}
