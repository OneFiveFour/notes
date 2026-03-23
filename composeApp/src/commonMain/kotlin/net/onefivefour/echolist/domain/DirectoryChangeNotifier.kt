package net.onefivefour.echolist.domain

import kotlinx.coroutines.flow.SharedFlow

/**
 * Domain-level abstraction for signaling that a directory's contents have changed.
 * Repositories that mutate directory contents call [notifyChanged],
 * and consumers (e.g. ViewModels) observe [directoryChanged].
 */
interface DirectoryChangeNotifier {
    val directoryChanged: SharedFlow<String>
    suspend fun notifyChanged(path: String)
}
