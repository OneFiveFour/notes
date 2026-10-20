package net.onefivefour.echolist.core.notifications.domain

fun interface TaskCompletionHandler {
    suspend fun complete(taskListId: String, taskId: String): Result<Unit>
    suspend fun completeOccurrence(taskListId: String, taskId: String, dueDate: String): Result<Unit> =
        complete(taskListId, taskId)
}