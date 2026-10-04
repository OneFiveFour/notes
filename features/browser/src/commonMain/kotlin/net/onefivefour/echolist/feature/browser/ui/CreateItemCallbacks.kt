package net.onefivefour.echolist.feature.browser.ui

internal data class CreateItemCallbacks(
    val onCreateFolder: () -> Unit = {},
    val onCreateNote: () -> Unit = {},
    val onCreateTaskList: () -> Unit = {}
)
