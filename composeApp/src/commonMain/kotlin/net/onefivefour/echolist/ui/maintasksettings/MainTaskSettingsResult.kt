package net.onefivefour.echolist.ui.maintasksettings

data class MainTaskSettingsResult(
    val mainTaskId: Long,
    val dueDate: String,
    val recurrence: String
)
