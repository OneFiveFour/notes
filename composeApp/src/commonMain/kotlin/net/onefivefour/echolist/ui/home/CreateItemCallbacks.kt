package net.onefivefour.echolist.ui.home

data class CreateItemCallbacks(
    val onCreateFolder: () -> Unit = {},
    val onCreateNote: () -> Unit = {},
    val onCreateTaskList: () -> Unit = {}
)
