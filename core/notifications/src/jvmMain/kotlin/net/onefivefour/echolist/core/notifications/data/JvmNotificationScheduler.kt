package net.onefivefour.echolist.core.notifications.data

import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/** Immediate delivery; scheduling and history are owned by NotificationCoordinator. */
internal class JvmNotificationScheduler {
    internal fun showNow(title: String, body: String) = showNotification(title, body)

    private fun showNotification(title: String, body: String) {
        if (SystemTray.isSupported()) {
            try {
                val tray = SystemTray.getSystemTray()
                val existingIcon = tray.trayIcons.firstOrNull()
                val trayIcon = existingIcon ?: createTrayIcon(tray)
                trayIcon.displayMessage(title, body, TrayIcon.MessageType.INFO)
            } catch (e: Exception) {
                println("[JvmNotificationScheduler] WARNING: SystemTray notification failed: ${e.message}")
                showFallbackNotification(title, body)
            }
        } else {
            println("[JvmNotificationScheduler] WARNING: SystemTray not supported, using fallback dialog")
            showFallbackNotification(title, body)
        }
    }

    private fun createTrayIcon(tray: SystemTray): TrayIcon {
        val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        val trayIcon = TrayIcon(image, "EchoList")
        trayIcon.isImageAutoSize = true
        tray.add(trayIcon)
        return trayIcon
    }

    private fun showFallbackNotification(title: String, body: String) {
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(
                null,
                body,
                title,
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }
}