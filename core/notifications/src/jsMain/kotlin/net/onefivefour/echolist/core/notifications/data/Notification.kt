package net.onefivefour.echolist.core.notifications.data

/** Browser Notification API; delivery history is managed by NotificationCoordinator. */
external class Notification(title: String, options: dynamic = definedExternally) {
    companion object {
        val permission: String
        fun requestPermission(): dynamic
    }
}