package net.onefivefour.echolist.core.notifications.domain

fun interface TaskCompletionHandler {
    suspend fun complete(taskListId: String, taskId: String): Result<Unit>
}
