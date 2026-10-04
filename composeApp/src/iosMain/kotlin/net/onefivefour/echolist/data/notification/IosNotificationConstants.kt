package net.onefivefour.echolist.data.notification

/**
 * Shared identifiers for iOS task-reminder notifications, used by both the
 * scheduler (which registers the category/action and attaches userInfo) and the
 * delegate (which handles the "Done" action response).
 */
internal object IosNotificationConstants {
    const val CATEGORY_ID = "echolist_tasks"
    const val HIDDEN_PREVIEW_PLACEHOLDER = "You have a task reminder"
    const val DONE_ACTION_ID = "echolist_task_done"
    const val DONE_ACTION_TITLE = "Done"
    const val USER_INFO_TASK_ID = "task_id"
    const val USER_INFO_TASK_LIST_ID = "task_list_id"
}
