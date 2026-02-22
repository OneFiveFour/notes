package net.onefivefour.echolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.source.SecureStorage
import net.onefivefour.echolist.data.source.StorageKeys
import net.onefivefour.echolist.network.auth.AuthEvent

class AuthViewModel(
    private val secureStorage: SecureStorage,
    private val authEvents: MutableSharedFlow<AuthEvent>
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Check storage for existing access token
        val token = secureStorage.get(StorageKeys.ACCESS_TOKEN)
        _authState.value = if (token != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }

        // Collect auth events from the interceptor
        viewModelScope.launch {
            authEvents.collect { event ->
                when (event) {
                    AuthEvent.ReAuthRequired -> {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            }
        }
    }

    fun onAuthenticated() {
        _authState.value = AuthState.Authenticated
    }
}
