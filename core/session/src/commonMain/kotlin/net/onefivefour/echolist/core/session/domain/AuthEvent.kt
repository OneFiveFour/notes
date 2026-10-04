package net.onefivefour.echolist.core.session.domain

/**
 * Events emitted by the auth layer to signal authentication state changes.
 */
sealed interface AuthEvent {
    data object ReAuthRequired : AuthEvent
}
