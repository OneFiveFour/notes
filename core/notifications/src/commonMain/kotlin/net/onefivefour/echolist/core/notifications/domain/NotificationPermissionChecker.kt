package net.onefivefour.echolist.core.notifications.domain

/**
 * Platform-agnostic contract for querying the current notification
 * permission status.
 */
interface NotificationPermissionChecker {
    /**
     * Returns true if notification permission is currently granted
     * on the host platform.
     */
    suspend fun isGranted(): Boolean
}