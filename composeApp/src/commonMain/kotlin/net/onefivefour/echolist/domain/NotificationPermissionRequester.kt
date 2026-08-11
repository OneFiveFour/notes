package net.onefivefour.echolist.domain

/**
 * Platform-agnostic contract for requesting notification permission from the user.
 */
interface NotificationPermissionRequester {
    /**
     * Request notification permission from the user.
     *
     * @return true if permission was granted, false if denied or an error occurred
     */
    suspend fun request(): Boolean
}
