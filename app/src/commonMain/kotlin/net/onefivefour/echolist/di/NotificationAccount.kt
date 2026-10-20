package net.onefivefour.echolist.di

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.session.domain.StorageKeys

/** Existing logins receive a stable device-local scope; new logins identify backend and username. */
internal class NotificationAccount(private val storage: SecureStorage) : NotificationAccountProvider {
    @OptIn(ExperimentalUuidApi::class)
    override fun currentAccountId(): String? {
        if (storage.get(StorageKeys.ACCESS_TOKEN) == null) return null
        return storage.get(StorageKeys.NOTIFICATION_ACCOUNT_ID) ?: Uuid.random().toString().also {
            storage.put(StorageKeys.NOTIFICATION_ACCOUNT_ID, it)
        }
    }
}