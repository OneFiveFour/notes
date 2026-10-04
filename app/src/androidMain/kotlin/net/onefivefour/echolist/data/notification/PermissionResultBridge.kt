package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridge between ViewModel-layer coroutines and Activity-lifecycle permission handling.
 *
 * The ViewModel (via [AndroidNotificationPermissionRequester]) calls [awaitPermission]
 * which suspends. The Activity observes [requests], handles the full permission flow
 * (rationale → system dialog → settings redirect if needed), then delivers the result.
 */
class PermissionResultBridge {
    private var pendingResult: CompletableDeferred<Boolean>? = null
    private val _requests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val requests: SharedFlow<Unit> = _requests.asSharedFlow()

    /**
     * Called by the requester. Suspends until the Activity handles the permission flow.
     *
     * @return true if permission was ultimately granted, false otherwise.
     */
    suspend fun awaitPermission(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingResult = deferred
        _requests.emit(Unit)
        return deferred.await()
    }

    /**
     * Called by the Activity after the permission result is known.
     */
    fun deliverResult(granted: Boolean) {
        pendingResult?.complete(granted)
        pendingResult = null
    }

    /**
     * Cancel a pending request (e.g., Activity destroyed).
     */
    fun cancel() {
        pendingResult?.complete(false)
        pendingResult = null
    }
}
