package net.onefivefour.echolist.core.notifications.data

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.onefivefour.echolist.core.notifications.database.NotificationDatabase
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.ReminderStore

internal val reminderJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Single-row snapshots make preferences, occurrences and sync checkpoints atomic across process death. */
internal class SqlReminderStore(private val database: NotificationDatabase) : ReminderStore {
    override suspend fun read(): ReminderState = withContext(Dispatchers.Default) {
        database.notificationStateQueries.readState().awaitAsOneOrNull()?.let {
            reminderJson.decodeFromString<ReminderState>(it)
        } ?: ReminderState()
    }

    override suspend fun write(state: ReminderState): Unit = withContext(Dispatchers.Default) {
        database.notificationStateQueries.writeState(reminderJson.encodeToString(state))
        Unit
    }
}