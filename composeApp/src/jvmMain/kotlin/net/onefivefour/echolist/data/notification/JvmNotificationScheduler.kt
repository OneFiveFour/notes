package net.onefivefour.echolist.data.notification

import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import kotlin.time.Clock
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.domain.NotificationScheduler

/**
 * JVM Desktop implementation of [NotificationScheduler].
 *
 * Uses [SystemTray] notifications when supported, otherwise falls back to a
 * coroutine-delayed [JOptionPane] dialog on the EDT.
 */
class JvmNotificationScheduler(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : NotificationScheduler {

    private val scheduledJobs = ConcurrentHashMap<String, Job>()

    override suspend fun schedule(
        taskId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        val dueDate = runCatching { LocalDate.parse(dueDateIso) }.getOrNull()
        if (dueDate == null) {
            println("[JvmNotificationScheduler] WARNING: Cannot parse due date '$dueDateIso' for task $taskId")
            return
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (dueDate < today) {
            println("[JvmNotificationScheduler] WARNING: Due date $dueDateIso is before today for task $taskId, skipping")
            return
        }

        // Cancel any existing job for the same task (idempotent replacement)
        scheduledJobs.remove(taskId)?.cancel()

        val dueDateInstant = dueDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val now = Clock.System.now()
        val delayDuration: Duration = dueDateInstant - now

        val job = scope.launch {
            if (delayDuration.isPositive()) {
                delay(delayDuration)
            }
            showNotification(title, body)
        }

        scheduledJobs[taskId] = job
    }

    override suspend fun cancel(taskId: String) {
        scheduledJobs.remove(taskId)?.cancel()
    }

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
