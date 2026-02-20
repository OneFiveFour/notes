package net.onefivefour.notes.network.auth

/**
 * Events emitted by the auth layer to signal authentication state changes.
 */
sealed interface AuthEvent {
    data object ReAuthRequired : AuthEvent
}
