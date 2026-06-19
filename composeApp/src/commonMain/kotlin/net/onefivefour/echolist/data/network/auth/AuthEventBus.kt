package net.onefivefour.echolist.data.network.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Concrete DI type for auth events so Koin never confuses this flow with
 * other generic MutableSharedFlow registrations after JVM type erasure.
 */
class AuthEventBus(
    private val eventFlow: MutableSharedFlow<AuthEvent> = MutableSharedFlow()
) {

    val events: SharedFlow<AuthEvent> = eventFlow.asSharedFlow()

    suspend fun emit(event: AuthEvent) {
        eventFlow.emit(event)
    }
}
